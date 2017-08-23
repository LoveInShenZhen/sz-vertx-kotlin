package controllers.builtin

import io.ebean.dbmigration.model.CurrentModel
import io.ebeaninternal.api.SpiEbeanServer
import sz.DB
import sz.DbIndex
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController

//
// Created by kk on 17/8/23.
//

@Comment("内置的Ebean工具接口方法")
class szebean : ApiController() {

    @Comment("生成创建/更新索引的SQL语句")
    fun CreateIndexSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = "") : String {
        val db = DB.byDataSource(dataSource)
        this.contentType("text/plain; charset=UTF-8")
        return DbIndex(db).GetCreateIndexSql()
    }

    @Comment("生成创建数据库表结构的SQL")
    fun CreateTablesSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = "") : String {
        val spiServer = DB.byDataSource(dataSource) as SpiEbeanServer
        val ddl = CurrentModel(spiServer)
        this.contentType("text/plain; charset=UTF-8")
        return ddl.createDdl
    }

    @Comment("生成删除数据库表结构的SQL")
    fun DropTablesSql(@Comment("数据源名称, 为空时,表示默认数据源") dataSource: String = "") : String {
        val spiServer = DB.byDataSource(dataSource) as SpiEbeanServer
        val ddl = CurrentModel(spiServer)
        this.contentType("text/plain; charset=UTF-8")
        return ddl.dropAllDdl
    }

}