package sz

import io.ebean.Ebean
import io.ebean.EbeanServer
import io.ebean.TxScope
import io.ebean.annotation.TxIsolation
import sz.annotations.DBIndexed
import sz.scaffold.ext.camelCaseToLowCaseSeprated
import sz.scaffold.tools.BizLogicException
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Table
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
            val sql = String.format("show index from `%s`", tableName)
            val rows = dbServer.createSqlQuery(sql).findList()
            for (row in rows) {
                val columnName = row.getString("Column_name")
                val indexName = row.getString("Key_name")

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
