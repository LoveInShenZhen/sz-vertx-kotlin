package sz.entityBean

import io.ebean.ExpressionList
import io.ebean.Finder
import io.ebean.bean.EntityBean
import sz.ebean.DB
import sz.scaffold.tools.logger.Logger
import javax.persistence.MappedSuperclass

//
// Created by kk on 2019-05-06.
//
@MappedSuperclass
abstract class BaseModel {

    private var _dataSource: String = DB.currentDataSource()

    fun dataSource(dataSource: String) {
        this._dataSource = dataSource
    }

    fun markAsDirty(dataSource: String = this._dataSource) {
        DB.byDataSource(dataSource).markAsDirty(this)
    }

    fun markPropertyUnset(propertyName: String) {
        (this as EntityBean)._ebean_getIntercept().setPropertyLoaded(propertyName, false)
    }

    fun save(dsName: String = this._dataSource) {
        DB.byDataSource(dsName).save(this)
    }

    fun flush(dsName: String = this._dataSource) {
        DB.byDataSource(dsName).flush()
    }

    fun update(dsName: String = this._dataSource) {
        DB.byDataSource(dsName).update(this)
    }

    fun insert(dsName: String = this._dataSource) {
        DB.byDataSource(dsName).insert(this)
    }

    fun delete(dsName: String = this._dataSource): Boolean {
        return DB.byDataSource(dsName).delete(this)
    }

    fun deletePermanent(dsName: String = this._dataSource): Boolean {
        return DB.byDataSource(dsName).deletePermanent(this)
    }

    fun refresh(dsName: String = this._dataSource) {
        return DB.byDataSource(dsName).refresh(this)
    }

    companion object {
        inline fun <reified I, reified T> finder(dsName: String): Finder<I, T> {
            Logger.debug("create finder for ${T::class.java.name}, dataSource: $dsName")
            return Finder(T::class.java, dsName)
        }
    }
}

