package sz

import io.ebean.Ebean
import io.ebean.EbeanServer
import io.ebean.TxScope
import io.ebean.annotation.TxIsolation
import io.ebeaninternal.server.transaction.DefaultTransactionThreadLocal
import io.ebeaninternal.server.transaction.TransactionMap
import kotlinx.coroutines.*
import org.apache.commons.lang3.reflect.FieldUtils
import sz.annotations.DBIndexed
import sz.coroutines.AutoThreadLocalElement
import sz.coroutines.asAutoContextElement
import sz.scaffold.ext.camelCaseToLowCaseSeprated
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.logger.Logger
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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
        return Ebean.getServer(dsName)!!
    }

    fun RunInTransaction(dataSource: String = "", body: (ebeanServer: EbeanServer) -> Unit) {
        val ebserver = Ebean.getServer(dataSource)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        ebserver.execute(txScope) { body(ebserver) }
    }

    fun <T> RunInTransaction(dataSource: String = "", body: (ebeanServer: EbeanServer) -> T): T {
        val ebserver = Ebean.getServer(dataSource)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return ebserver.executeCall(txScope) { body(ebserver) }
    }

    // 创建一个新的 ebean(事务相关) 协程上下文
    fun ebeanCoroutineContext(): AutoThreadLocalElement<TransactionMap> {
        return ebeanTransactionMap.asAutoContextElement(TransactionMap())
    }

    @Suppress("UNCHECKED_CAST")
    private fun getThreadLocalTransactionMap(): ThreadLocal<TransactionMap> {
        val field = FieldUtils.getField(DefaultTransactionThreadLocal::class.java, "local", true)
        return FieldUtils.readStaticField(field, true) as ThreadLocal<TransactionMap>
    }

    private val ebeanTransactionMap = getThreadLocalTransactionMap()
}

fun CoroutineScope.launchWithEbean(context: CoroutineContext = EmptyCoroutineContext,
                                   block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(context = context + DB.ebeanCoroutineContext(), block = block)
}

fun <T> CoroutineScope.asyncEbean(context: CoroutineContext = EmptyCoroutineContext,
                                  block: suspend CoroutineScope.() -> T): Deferred<T> {
    return this.async(context = context + DB.ebeanCoroutineContext(), block = block)
}

fun BigDecimal?.safeValue(): BigDecimal {
    return this ?: BigDecimal.ZERO
}

fun EbeanServer.RunInTransaction(body: () -> Unit) {
    val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
    this.execute(txScope, body)
}

fun <T> EbeanServer.RunInTransaction(body: () -> T): T {
    val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
    return this.executeCall(txScope, body)
}

fun EbeanServer.TableExists(tableName: String): Boolean {
    val rows = this.createSqlQuery("SHOW TABLES").findList()
    val count = rows.count { it.values.first() == tableName }
    return count > 0
}

suspend fun <R> EbeanServer.callWithTransaction(body: () -> R): R {
    val db = this
    return coroutineScope {
        withContext(this.coroutineContext + DB.ebeanCoroutineContext()) {
            val tran = db.beginTransaction()
            try {
                val result = body()
                tran.commit()
                Logger.debug("commit: $tran")
                return@withContext result
            } catch (e: Exception) {
                tran.rollback(e)
                Logger.debug("rollback(e): $tran")
                throw e
            } finally {
                tran.end()
            }
        }
    }
}

suspend fun EbeanServer.runWithTransaction(body: () -> Unit) {
    val db = this
    coroutineScope {
        withContext(this.coroutineContext + DB.ebeanCoroutineContext()) {
            val tran = db.beginTransaction()
            try {
                val result = body()
                tran.commit()
                Logger.debug("commit: $tran")
                return@withContext result
            } catch (e: Exception) {
                tran.rollback(e)
                Logger.debug("rollback(e): $tran")
                throw e
            } finally {
                tran.end()
            }
        }
    }
}
