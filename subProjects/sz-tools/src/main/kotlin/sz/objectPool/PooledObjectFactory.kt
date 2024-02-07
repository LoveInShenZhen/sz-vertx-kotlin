package sz.objectPool

import sz.logger.log

//
// Created by kk on 2019/10/23.
//
abstract class PooledObjectFactory<T : Any> {

    abstract suspend fun createObjectAwait(): T

    open suspend fun wrapObject(pool: ObjectPool<T>): PooledObject<T>? {
        try {
            val obj = createObjectAwait()
            return PooledObject(target = obj, pool = pool)
        } catch (ex: Exception) {
            log.warn("Create ${this.javaClass.name} object failed.\n$ex")
            return null
        }

    }

    open fun destroyObject(target: T) {

    }
}