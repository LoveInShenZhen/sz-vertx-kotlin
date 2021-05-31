package sz.ebean.gen.dbinfo

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jodd.util.StringUtil

//
// Created by kk on 2021/5/5.
//
class TableInfo {
    var table_name = ""
    var table_type = ""
    var columns = mutableListOf<ColumnInfo>()
    var pk_columns = mutableSetOf<String>()
    var indexs = mutableListOf<IndexInfo>()

    val class_name: String
        get() {
            return StringUtil.toCamelCase(this.table_name, true, '_')
        }

    override fun toString(): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }

    fun isUnique(columnName: String): Boolean {
        return this.indexs.filter { it.column_name == columnName }.any { it.unique }
    }

    companion object {
        private val mapper = YAMLMapper().registerKotlinModule()
    }
}