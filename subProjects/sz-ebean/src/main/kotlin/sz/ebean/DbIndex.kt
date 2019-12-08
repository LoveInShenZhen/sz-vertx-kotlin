package sz.ebean

import io.ebean.Ebean
import io.ebean.EbeanServer
import sz.annotations.DBIndexed
import sz.scaffold.ext.camelCaseToLowCaseSeprated
import sz.scaffold.tools.logger.Logger
import kotlin.reflect.full.memberProperties

//
// Created by kk on 2019/11/7.
//
internal class IndexInfo(var indexName: String) {

    var columns: MutableSet<String> = mutableSetOf()

    fun addClumn(columnName: String) {
        this.columns.add(columnName)
    }

    fun isCombinedIndex(): Boolean {
        return this.columns.size > 1
    }

    companion object {

        fun loadIndexInfoForTable(tableName: String, dbServer: EbeanServer): Map<String, IndexInfo> {
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
                    indexMap[indexName] = IndexInfo(indexName)
                }

                val indexInfo = indexMap[indexName]
                indexInfo!!.addClumn(columnName)
            }

            return indexMap
        }

        // 判断指定的字段是否有索引, 排除联合索引
        fun indexExists(indexMap: Map<String, IndexInfo>, columnName: String): Boolean {
            for ((_, indexInfo) in indexMap) {

                if (indexInfo.isCombinedIndex()) continue

                if (indexInfo.columns.contains(columnName)) return true
            }
            return false
        }
    }

}

@Suppress("LocalVariableName")
class DbIndex(private val dbServer: EbeanServer) {

    fun createIndexSql(): String {
        val sb = StringBuilder()
        val classes = SzEbeanConfig.ebeanServerConfigs.get(dbServer.name)!!.classes
        for (modelClass in classes) {
            if (isEntityClass(modelClass)) {
                val tableName = getTableName(modelClass)
                val dropSql = dropIndexSqlBy(modelClass)
                val createSql = createIndexByModelSql(modelClass)
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
        sb.append("-- 修改表的默认字符集和所有列的字符集为 utf8mb4\n")
        val classes = SzEbeanConfig.ebeanServerConfigs.get(dbServer.name)!!.classes
        for (modelClass in classes) {
            if (isEntityClass(modelClass)) {
                val tableName = getTableName(modelClass)
                sb.append("alter table `$tableName` convert to character set utf8mb4;\n")
            }
        }
        return sb.toString()
    }

    private fun getIndexedFieldNames(modelClass: Class<*>): Set<String> {
        return modelClass.kotlin.memberProperties
            .filter { it.annotations.filter { it is DBIndexed }.isNotEmpty() }
            .map { it.name.camelCaseToLowCaseSeprated('_') }
            .toSet()
    }

    private fun indexedColumns(tableName: String): Map<String, String> {
        val sql = String.format("show index from `%s`", tableName)
        val rows = Ebean.createSqlQuery(sql).findList()

        return rows.map {
            Pair(it.getString("Column_name"), it.getString("Key_name"))
        }.toMap()
    }


    private fun createIndexByModelSql(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val fieldNames = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.loadIndexInfoForTable(tableName, dbServer)
        for (fieldName in fieldNames) {
            if (!IndexInfo.indexExists(indexMap, fieldName)) {
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

    private fun dropIndexSqlBy(modelClass: Class<*>): String {
        val tableName = getTableName(modelClass)
        val indexedFields = getIndexedFieldNames(modelClass)

        val sb = StringBuilder()

        val indexMap = IndexInfo.loadIndexInfoForTable(tableName, dbServer)
        for (indexInfo in indexMap.values) {
            if (indexInfo.isCombinedIndex()) {
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