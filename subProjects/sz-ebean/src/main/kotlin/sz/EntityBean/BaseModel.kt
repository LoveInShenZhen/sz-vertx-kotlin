package sz.EntityBean

import io.ebean.Finder
import io.ebean.bean.EntityBean
import sz.DB
import sz.scaffold.tools.logger.Logger
import javax.persistence.MappedSuperclass

//
// Created by kk on 2019-05-06.
//
@MappedSuperclass
abstract class BaseModel {

    fun markAsDirty(dsName: String = DB.currentDataSource()) {
        DB.byDataSource(dsName).markAsDirty(this)
    }

    fun markPropertyUnset(propertyName: String) {
        (this as EntityBean)._ebean_getIntercept().setPropertyLoaded(propertyName, false)
    }

    fun save(dsName: String = DB.currentDataSource()) {
        DB.byDataSource(dsName).save(this)
    }

    fun flush(dsName: String = DB.currentDataSource()) {
        DB.byDataSource(dsName).flush()
    }

    fun update(dsName: String = DB.currentDataSource()) {
        DB.byDataSource(dsName).update(this)
    }

    fun insert(dsName: String = DB.currentDataSource()) {
        DB.byDataSource(dsName).insert(this)
    }

    fun delete(dsName: String = DB.currentDataSource()): Boolean {
        return DB.byDataSource(dsName).delete(this)
    }

    fun deletePermanent(dsName: String = DB.currentDataSource()): Boolean {
        return DB.byDataSource(dsName).deletePermanent(this)
    }

    fun refresh(dsName: String = DB.currentDataSource()) {
        return DB.byDataSource(dsName).refresh(this)
    }

    companion object {
        inline fun <reified I, reified T> finder(dsName: String): Finder<I, T> {
            Logger.debug("create finder for ${T::class.java.name}, dataSource: $dsName")
            return Finder(T::class.java, dsName)
        }
    }
}

