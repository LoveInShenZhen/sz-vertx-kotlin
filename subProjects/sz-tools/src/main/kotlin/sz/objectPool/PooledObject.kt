package sz.objectPool

import java.io.Closeable

//
// Created by kk on 2019/10/23.
//
class PooledObject<T>(val obj: T, private val pool: ObjectPool<T>) : Closeable {

    internal var status = PooledObjectStatus.Idle
    internal val createTimeMs = System.currentTimeMillis()
    internal var lastBorrowTimeMs = createTimeMs
    internal var lastReturnTimeMs = createTimeMs

    private var _broken = false

    val broken: Boolean
        get() = _broken

    fun markBroken() {
        _broken = true
    }

    val identityHashCode: Int by lazy {
        System.identityHashCode(obj)
    }

    override fun close() {
        if (status == PooledObjectStatus.Using) {
            status = PooledObjectStatus.Returning
            val pooledObj = this
            pool.returnObject(pooledObj)
        }
    }

}

enum class PooledObjectStatus {
    Idle,
    Using,
    Returning,
    Broken,
}