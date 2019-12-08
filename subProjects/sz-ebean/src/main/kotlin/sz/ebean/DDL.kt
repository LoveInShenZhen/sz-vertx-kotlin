package sz.ebean

import io.ebeaninternal.api.SpiEbeanServer
import io.ebeaninternal.dbmigration.model.CurrentModel

//
// Created by kk on 2019/11/14.
//
object DDL {

    fun createDdl(dataSource: String = ""): String {
        val spiServer = DB.byDataSource(dataSource) as SpiEbeanServer
        val modelDdl = CurrentModel(spiServer)

        val sqlScript = modelDdl.createDdl
//        val pos = sqlScript.indexOf("create table")

        return sqlScript
    }

    fun dropAllDdl(dataSource: String = ""): String {
        val spiServer = DB.byDataSource(dataSource) as SpiEbeanServer
        val ddl = CurrentModel(spiServer)
        return ddl.dropAllDdl
    }

    fun createIndexDdl(dataSource: String = ""): String {
        val db = DB.byDataSource(dataSource)
        return DbIndex(db).createIndexSql()
    }
}