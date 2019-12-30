package controllers.builtin

import sz.ebean.DB
import sz.ebean.DDL
import sz.ebean.runTransactionAwait
import sz.scaffold.annotations.Comment
import sz.scaffold.aop.interceptors.builtin.api.DevModeOnly
import sz.scaffold.controller.ApiController

//
// Created by kk on 17/8/23.
//

@Suppress("FunctionName", "ClassName")
@Comment("内置的Ebean工具接口方法")
class szebean : ApiController() {

    @Comment("生成创建/更新索引的SQL语句")
    @DevModeOnly
    suspend fun CreateIndexSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = ""): String {
        this.contentType("text/plain; charset=UTF-8")
        return DB.byDataSource(dataSource).runTransactionAwait {
            DDL.createIndexDdl(dataSource)
        }
    }

    @Comment("生成创建数据库表结构的SQL")
    @DevModeOnly
    suspend fun CreateTablesSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = ""): String {
        this.contentType("text/plain; charset=UTF-8")
        return DB.byDataSource(dataSource).runTransactionAwait {
            DDL.createDdl(dataSource)
        }
    }

    @Comment("生成删除数据库表结构的SQL")
    @DevModeOnly
    suspend fun DropTablesSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = ""): String {
        this.contentType("text/plain; charset=UTF-8")
        return DB.byDataSource(dataSource).runTransactionAwait {
            DDL.dropAllDdl(dataSource)
        }
    }

    @Comment("清空数据库,重新创建表")
    @DevModeOnly
    suspend fun evolution(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = ""): String {
        this.contentType("text/plain; charset=UTF-8")
        return DB.byDataSource(dataSource).runTransactionAwait {
            it.sqlUpdate(DDL.dropAllDdl(dataSource)).execute()
            it.sqlUpdate(DDL.createDdl(dataSource)).execute()
            it.sqlUpdate(DDL.createIndexDdl(dataSource)).execute()

            "数据库重置成功"
        }
    }

}