package sz

import io.ebean.Ebean
import io.ebean.EbeanServer
import io.ebean.SqlRow
import io.ebean.TxScope
import io.ebean.annotation.TxIsolation
import io.ebeaninternal.server.transaction.DefaultTransactionThreadLocal
import io.ebeaninternal.server.transaction.TransactionMap
import jodd.bean.BeanUtil
import jodd.datetime.JDateTime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.reflect.FieldUtils
import sz.annotations.DBIndexed
import sz.coroutines.AutoThreadLocalElement
import sz.coroutines.asAutoContextElement
import sz.scaffold.ext.camelCaseToLowCaseSeprated
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.logger.Logger
import java.math.BigDecimal
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.concurrent.getOrSet
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

internal class IndexInfo(var indexName: String) {

    var columns: MutableSet<String> = mutableSetOf()

    fun AddClumn(columnName: String) {
        this.columns.add(columnName)
    }

    fun IsCombinedIndex(): Boolean {
        return this.columns.size > 1
    }

    companion object {

        fun LoadIndexInfoForTable(tableName: String, dbServer: EbeanServer): Map<String, IndexInfo> {
            val indexMap = mutableMapOf<String, IndexInfo>()
            var sql = ""
            if (SzEbeanConfig.isMySql()) {
                sql = "show index from `$tableName`"
            }
            if (SzEbeanConfig.isH2()) {
                sql = "SELECT t.TABLE_NAME, t.COLUMN_NAME AS Column_name, t.INDEX_NAME AS Key_name FROM INFORMATION_SCHEMA.INDEXES as t WHERE t.INDEX_TYPE_NAME != 'PRIMARY KEY' AND t.TABLE_NAME = '${tableName.toUpperCase()}'"
            }
            val rows = dbServer.createSqlQuery(sql).findList()
            for (row in rows) {
                val columnName = row.getString("Column_name")
                val indexName = row.getString("Key_name")

                Logger.debug("Column_name: $columnName  Key_name: $indexName")

                if (!indexMap.containsKey(indexName)) {
                    indexMap.put(indexName, IndexInfo(indexName))
                }

                val indexInfo = indexMap.get(indexName)
                indexInfo!!.AddClumn(columnName)
            }

            return indexMap
        }

        // 判断指定的字段是否有索引, 排除联合索引
        fun IndexExists(indexMap: Map<String, IndexInfo>, columnName: String): Boolean {
            for ((_, indexInfo) in indexMap) {

                if (indexInfo.IsCombinedIndex()) continue

                if (indexInfo.columns.contains(columnName)) return true
            }
            return false
        }
    }

}

class DbIndex(private val dbServer: EbeanServer) {

    fun GetCreateIndexSql(): String {
        val sb = StringBuilder()
        val classes = SzEbeanConfig.ebeanServerConfigs.get(dbServer.name)!!.classes
        for (modelClass in classes) {
            if (isEntityClass(modelClass)) {
                val tableName = getTableName(modelClass)
                val dropSql = getDropIndexSqlBy(modelClass)
                val createSql = getCreateIndexSqlByModel(modelClass)
                if ((dropSql + createSql).isNotBlank()) {
                    sb.append("-- ").append("================================\n")
                    sb.append("-- ").append("Table: ").append(tableName).append("\n")
                    sb.append("-- ").append("================================\n")
                    sb.append(dropSql)
                    sb.append("\n")
                    sb.append(createSql)
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }

    fun alterTableUseUtf8mb4(): String {
        val sb = StringBuilder()
        sb.append("# 修改表的默认字符集和所有列的字符集为 utf8mb4\n")
        val classes = SzEbeanConfig.ebeanServerConfigs.get(dbServer.name)!!.classes
        for (modelClass in classes) {
            if (isEntityClass(modelClass)) {
                val tableName = getTableName(modelClass)
                sb.append("alter table `$tableName` convert to character set utf8mb4;\n")
            }
        }
        return sb.toString()
    }


    private fun isEntityClass(modelClass: Class<*>): Boolean {
        val annoEntity = modelClass.getAnnotation(Entity::class.java)
        return annoEntity != null
    }

    private fun getTableName(modelClass: Class<*>): String {
        modelClass.getAnnotation(Entity::class.java) ?: throw BizLogicException("不是实体类")

        val annoTable = modelClass.getAnnotation(Table::class.java)
        if (annoTable != null && annoTable.name.isNotBlank()) {
            return annoTable.name
        } else {
            return modelClass.simpleName.camelCaseToLowCaseSeprated('_')
        }

    }

    private fun getIndexedFieldNames(modelClass: Class<*>): Set<String> {
        return modelClass.kotlin.memberProperties
            .filter { it.annotations.filter { it is DBIndexed }.isNotEmpty() }
            .map { it.name.camelCaseToLowCaseSeprated('_') }
            .toSet()
    }

    private fun getIndexedColumns(tableName: String): Map<String, String> {
        val sql = String.format("show index from `%s`", tableName)
        val rows = Ebean.createSqlQuery(sql).findList()

        return rows.map {
            Pair(it.getString("Column_name"), it.getString("Key_name"))
        }.toMap()
    }


    private fun getCreateIndexSqlByModel(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val fieldNames = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.LoadIndexInfoForTable(tableName, dbServer)
        for (fieldName in fieldNames) {
            if (!IndexInfo.IndexExists(indexMap, fieldName)) {
                // 对应的字段索引不存在
                val idx_name = String.format("idx_%s_%s", tableName, fieldName)
                val create_sql = String.format("CREATE INDEX `%s` ON `%s` (`%s`);",
                    idx_name,
                    tableName,
                    fieldName)
                sb.append(create_sql).append("\n")
            }
        }

        if (sb.isNotEmpty()) {
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun getDropIndexSqlBy(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val indexedFields = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.LoadIndexInfoForTable(tableName, dbServer)
        for (indexInfo in indexMap.values) {
            if (indexInfo.IsCombinedIndex()) {
                continue
            }

            if (indexInfo.indexName.toUpperCase() == "PRIMARY") {
                continue
            }

            if (indexInfo.indexName.startsWith("uq_")) {
                continue
            }

            if (indexInfo.columns.filter { it in indexedFields }.isEmpty()) {
                sb.append(String.format("DROP INDEX `%s` ON `%s`;\n",
                    indexInfo.indexName,
                    tableName))
            }
        }

        if (sb.isNotEmpty()) {
            sb.append("\n")
        }
        return sb.toString()
    }
}

object DB {

    fun Default(): EbeanServer {
        return Ebean.getDefaultServer()
    }

    fun byDataSource(dsName: String): EbeanServer {
        if (dsName.isBlank()) {
            return Ebean.getDefaultServer()
        }
        if (SzEbeanConfig.ebeanServerConfigs.containsKey(dsName).not()) {
            throw BizLogicException("""错误的 dataSource name: "$dsName", 请检查 application.conf 或者其他关于 dataSource 的配置项, 参数等等""")
        }
        return Ebean.getServer(dsName)!!
    }

    /**
     * 根据线程上下文, 获取当前正在使用的 EbeanServer 实例
     */
    fun byContext(): EbeanServer {
        return byDataSource(currentDataSource())
    }

    @Deprecated("""请改为使用 DB.byDataSource("dsName").runInTransactionAwait() """)
    fun RunInTransaction(dataSource: String = "", readOnly: Boolean = false, body: (ebeanServer: EbeanServer) -> Unit) {
        try {
            setCurrentDataSource(dataSource)
            val ebserver = byDataSource(dataSource)
            val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
            ebserver.execute(txScope) { body(ebserver) }
        } finally {
            resetCurrentDataSource()
        }
    }

    @Deprecated("""请改为使用 DB.byDataSource("dsName").callInTransactionAwait() """)
    fun <T> RunInTransaction(dataSource: String = "", readOnly: Boolean = false, body: (ebeanServer: EbeanServer) -> T): T {
        try {
            setCurrentDataSource(dataSource)
            val ebserver = byDataSource(dataSource)
            val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
            return ebserver.executeCall(txScope) { body(ebserver) }
        } finally {
            resetCurrentDataSource()
        }

    }

    // 创建一个新的 ebean transaction 协程上下文
    fun transactionCoroutineContext(): AutoThreadLocalElement<TransactionMap> {
        return ebeanTransactionMap.asAutoContextElement(TransactionMap())
    }

    fun dataSourceCoroutineContext(dsName: String): AutoThreadLocalElement<String> {
        return currentDataSourceName.asAutoContextElement(dsName)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getThreadLocalTransactionMap(): ThreadLocal<TransactionMap> {
        val field = FieldUtils.getField(DefaultTransactionThreadLocal::class.java, "local", true)
        return FieldUtils.readStaticField(field, true) as ThreadLocal<TransactionMap>
    }

    /**
     * 根据当前线程上下文, 返回正在使用的 数据源的名称
     */
    fun currentDataSource(): String {
        return currentDataSourceName.getOrSet { "" }
    }

    internal fun setCurrentDataSource(dsName: String) {
        currentDataSourceName.set(dsName)
    }

    fun resetCurrentDataSource() {
        currentDataSourceName.set("")
    }

    private val ebeanTransactionMap = getThreadLocalTransactionMap()
    private val currentDataSourceName = ThreadLocal<String>()
}

fun BigDecimal?.safeValue(): BigDecimal {
    return this ?: BigDecimal.ZERO
}

fun EbeanServer.RunInTransaction(readOnly: Boolean = false, body: () -> Unit) {
    try {
        DB.setCurrentDataSource(this.name)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        this.execute(txScope, body)
    } finally {
        DB.resetCurrentDataSource()
    }
}

suspend fun EbeanServer.runInTransactionAwait(readOnly: Boolean = false, body: () -> Unit) {
    val db = this
    coroutineScope {
        withContext(this.coroutineContext + DB.transactionCoroutineContext() + DB.dataSourceCoroutineContext(db.name) + SzEbeanConfig.ebeanCoroutineDispatcher) {
            val tran = db.beginTransaction(TxIsolation.READ_COMMITED)
            tran.isReadOnly = readOnly
            try {
                val result = body()
                tran.commit()
                return@withContext result
            } catch (e: Exception) {
                tran.rollback(e)
//                    Logger.debug("rollback: $tran for reason: ${e.message}")
                throw e
            } finally {
                tran.end()
            }
        }
    }
}

fun <T> EbeanServer.RunInTransaction(readOnly: Boolean = false, body: () -> T): T {
    try {
        DB.setCurrentDataSource(this.name)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        return this.executeCall(txScope, body)
    } finally {
        DB.resetCurrentDataSource()
    }
}

suspend fun <T> EbeanServer.callInTransactionAwait(readOnly: Boolean = false, body: () -> T): T {
    val db = this
    return coroutineScope {
        withContext(this.coroutineContext + DB.transactionCoroutineContext() + DB.dataSourceCoroutineContext(db.name) + SzEbeanConfig.ebeanCoroutineDispatcher) {
            val tran = db.beginTransaction(TxIsolation.READ_COMMITED)
            tran.isReadOnly = readOnly
            try {
                val result = body()
                tran.commit()
                return@withContext result
            } catch (e: Exception) {
                tran.rollback(e)
//                    Logger.debug("rollback: $tran for reason: ${e.message}")
                throw e
            } finally {
                tran.end()
            }
        }
    }
}

fun EbeanServer.tableExists(tableName: String): Boolean {
    val rows = this.createSqlQuery("SHOW TABLES").findList()
    val count = rows.count { it.values.first() == tableName }
    return count > 0
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <TBean : Any> SqlRow.toBean(beanClass: KClass<TBean>): TBean {
    val bean = beanClass.createInstance()
    val beanUtil = BeanUtil.pojo

    this.forEach { name, value ->
        if (beanUtil.hasProperty(bean, name)) {
            //                Logger.debug("==> name: $name, value: $value, type: ${value?.javaClass?.name}")
            val propValue = when (val propType = beanUtil.getPropertyType(bean, name)) {
                java.lang.String::class.java -> this.getString(name)
                Int::class.java, java.lang.Integer::class.java -> this.getInteger(name)
                Long::class.java, java.lang.Long::class.java -> this.getLong(name)
                Float::class.java, java.lang.Float::class.java -> this.getFloat(name)
                Double::class.java, java.lang.Double::class.java -> this.getDouble(name)
                Boolean::class.java, java.lang.Boolean::class.java -> this.getBoolean(name)
                BigDecimal::class.java -> this.getBigDecimal(name)
                JDateTime::class.java -> if (value == null) null else JDateTime(this.getUtilDate(name))
                Date::class.java -> this.getUtilDate(name)
                UUID::class.java -> this.getUUID(name)
                java.sql.Date::class.java -> this.getDate(name)
                java.sql.Timestamp::class.java -> this.getTimestamp(name)
                else -> throw BizLogicException("SqlRow.toBean() 方法不支持转换 Bean Class 中类型为 ${propType.name} 的属性, 请联系开发人员")
            }
            beanUtil.setProperty(bean, name, propValue)
        }
    }

    return bean
}

fun <TBean : Any> List<SqlRow>.toBeans(beanClass: KClass<TBean>): List<TBean> {
    return this.map { it.toBean(beanClass) }
}
