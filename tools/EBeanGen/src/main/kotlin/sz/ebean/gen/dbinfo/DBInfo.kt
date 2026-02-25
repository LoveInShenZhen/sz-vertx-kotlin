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
            var postgres_current_schema = ""
            if (metaData.url.startsWith("jdbc:postgresql:")) {
                val rs = cnn.createStatement().executeQuery("SELECT current_schema;")
                if (rs.next()) {
                    postgres_current_schema = rs.getString(1)
                }
            }

            while (tablesRs.next()) {
                if (metaData.url.startsWith("jdbc:postgresql:")) {
                    // 过滤掉非 public 模式下的表
                    if (tablesRs.getString("TABLE_SCHEM") != postgres_current_schema) {
                        continue
                    }
                }

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
            var postgres_current_schema = ""
            if (metaData.url.startsWith("jdbc:postgresql:")) {
                val rs = cnn.createStatement().executeQuery("SELECT current_schema;")
                if (rs.next()) {
                    postgres_current_schema = rs.getString(1)
                }
            }

            while (tablesRs.next()) {
                if (metaData.url.startsWith("jdbc:postgresql:")) {
                    // 过滤掉非 public 模式下的表
                    if (tablesRs.getString("TABLE_SCHEM") != postgres_current_schema) {
                        continue
                    }
                }

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

    /**
     * 从系统表, 查询所有的保留字, 并缓存起来;
     * mysql 方式
     * 查询回来的关键字都是大写
     */
    val reservedWords: Set<String> by lazy {
        val sql = "SELECT WORD FROM information_schema.KEYWORDS where RESERVED=1"
        return@lazy db.sqlQuery(sql).findList().map { it.getString("WORD") }.toSet()
    }

    /**
     * 判断一个单词是否是保留字
     */
    fun isReservedWord(word: String): Boolean {
        return reservedWords.contains(word.uppercase())
    }

    /**
     * 对一个单词进行引号包裹, 如果是保留字, 则包裹在反引号中;
     */
    fun quoteWord(word: String): String {
        return if (isReservedWord(word)) {
            "`$word`"
        } else {
            word
        }
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
                    remarks = columnRs.getString("REMARKS")?.trim() ?: ""
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