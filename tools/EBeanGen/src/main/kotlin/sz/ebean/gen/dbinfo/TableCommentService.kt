package sz.ebean.gen.dbinfo

import io.ebean.Database

//
// Created by kk on 2022/6/25.
//
interface TableCommentService {
    fun tableCommentOf(dbName: String, tableName: String): String
}

class MysqlTableCommentService(val db: Database) : TableCommentService {
    override fun tableCommentOf(dbName: String, tableName: String): String {
        val sql =
            "select TABLE_COMMENT from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=:db_name and TABLE_NAME=:table_name"
        val row = db.sqlQuery(sql)
            .setParameter("db_name", dbName)
            .setParameter("table_name", tableName)
            .findOne()
        if (row != null) {
            return row.getString("TABLE_COMMENT").trim()
        }

        return ""
    }
}

class PostgresTableCommentService(val db: Database) : TableCommentService {
    override fun tableCommentOf(dbName: String, tableName: String): String {
        val sql = "SELECT obj_description('public.${tableName}'::regclass) AS table_comment;"
        val row = db.sqlQuery(sql).findOne()
        if (row != null) {
            return row.getString("table_comment").trim()
        }

        return ""
    }
}