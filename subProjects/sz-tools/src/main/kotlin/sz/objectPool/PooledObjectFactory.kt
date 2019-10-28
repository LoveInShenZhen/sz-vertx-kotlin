package sz.objectPool

import sz.scaffold.tools.logger.Logger
import java.lang.Exception

//
// Created by kk on 2019/10/23.
//
abstract class PooledObjectFactory<T> {

    abstract fun createObject(): T

    fun wrapObject(pool: ObjectPool<T>): PooledObject<T>? {
        try {
            val obj = createObject()
            return PooledObject(obj = obj, pool = pool)
        } catch (ex:Exception) {
            Logger.warn("Create ${this.javaClass.name} object failed.\n$ex")
            return null
        }

    }

    open fun onDestoryObject(obj: T) {
//        Logger.debug("Destory pooled object: [${System.identityHashCode(obj)}]")
    }
}