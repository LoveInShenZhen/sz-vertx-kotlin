package sz.ebean.gen.dbinfo

import io.ebean.Database

//
// Created by kk on 2021/5/5.
//

class DBInfo(val db: Database, val tableCommentService: TableCommentService?) {

    fun tables(): List<TableInfo> {
        val tables = mutableListOf<TableInfo>()

        db.beginTransaction().use { tran ->
            val cnn = tran.connection()
            val metaData = cnn.metaData

            val tablesRs = metaData.getTables(cnn.catalog, null, null, arrayOf("TABLE"))
            while (tablesRs.next()) {
                val tableName = tablesRs.getString("TABLE_NAME")
                val tableType = tablesRs.getString("TABLE_TYPE")
                var remarks = tablesRs.getString("REMARKS")

                if (remarks.isNullOrBlank() && this.tableCommentService != null) {
                    remarks = tableCommentService.tableCommentOf(cnn.catalog, tableName)
                }

                val tableInfo = TableInfo().apply {
                    this.table_name = tableName
                    this.table_type = tableType
                    this.comment = remarks
                }

                if (tableType == "TABLE") {
                    tables.add(tableInfo)
                }
            }
        }

        tables.forEach { tb ->
            this.loadPKInfo(tb)
            this.loadIndexInfo(tb)
            this.loadColumnInfo(tb)
        }

        return tables
    }

    fun views(): List<TableInfo> {
        val tables = mutableListOf<TableInfo>()

        db.beginTransaction().use { tran ->
            val cnn = tran.connection()
            val metaData = cnn.metaData

            val tablesRs = metaData.getTables(cnn.catalog, null, null, arrayOf("VIEW"))
            while (tablesRs.next()) {
                val tableName = tablesRs.getString("TABLE_NAME")
                val tableType = tablesRs.getString("TABLE_TYPE")
                var remarks = tablesRs.getString("REMARKS")

                if (remarks.isNullOrBlank() && this.tableCommentService != null) {
                    remarks = tableCommentService.tableCommentOf(cnn.catalog, tableName)
                }

                val tableInfo = TableInfo().apply {
                    this.table_name = tableName
                    this.table_type = tableType
                    this.comment = remarks
                }

                if (tableType == "VIEW") {
                    tables.add(tableInfo)
                }
            }
        }

        tables.forEach { tb ->
            this.loadPKInfo(tb)
            this.loadIndexInfo(tb)
            this.loadColumnInfo(tb)
        }

        return tables
    }

    private fun loadColumnInfo(tableInfo: TableInfo): TableInfo {
        db.beginTransaction().use { tran ->
            val cnn = tran.connection()
            val metaData = cnn.metaData
            tableInfo.columns.clear()

            val columnRs = metaData.getColumns(cnn.catalog, null, tableInfo.table_name, null)
            while (columnRs.next()) {
                val columnInfo = ColumnInfo().apply {
                    column_name = columnRs.getString("COLUMN_NAME")
                    jdbc_type = columnRs.getInt("DATA_TYPE")
                    type_name = columnRs.getString("TYPE_NAME")
                    column_size = columnRs.getInt("COLUMN_SIZE")
                    null_able = columnRs.getString("IS_NULLABLE").uppercase() == "YES"
                    default_value = columnRs.getString("COLUMN_DEF")
                    is_autoincrement = columnRs.getString("IS_AUTOINCREMENT").uppercase() == "YES"
                    remarks = columnRs.getString("REMARKS").trim()
                    is_pk = tableInfo.pk_columns.contains(column_name)
                }
                tableInfo.columns.add(columnInfo)
            }
        }

        return tableInfo
    }

    private fun loadPKInfo(tableInfo: TableInfo): TableInfo {
        db.beginTransaction().use { tran ->
            val cnn = tran.connection()
            val metaData = cnn.metaData
            tableInfo.pk_columns.clear()

            val rs = metaData.getPrimaryKeys(cnn.catalog, null, tableInfo.table_name)
            while (rs.next()) {
                tableInfo.pk_columns.add(rs.getString("COLUMN_NAME"))
            }
        }

        return tableInfo
    }

    private fun loadIndexInfo(tableInfo: TableInfo): TableInfo {
        db.beginTransaction().use { tran ->
            val cnn = tran.connection()
            val metaData = cnn.metaData
            tableInfo.indexs.clear()

            val rs = metaData.getIndexInfo(cnn.catalog, null, tableInfo.table_name, false, false)
            while (rs.next()) {
                val indexInfo = IndexInfo().apply {
                    column_name = rs.getString("COLUMN_NAME")
                    non_unique = rs.getBoolean("NON_UNIQUE")
                }
                tableInfo.indexs.add(indexInfo)
            }
        }

        return tableInfo
    }
}