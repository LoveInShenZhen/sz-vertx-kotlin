package sz.objectPool

import jodd.datetime.ext.epochMsToLocalDateTime
import java.io.Closeable

//
// Created by kk on 2019/10/23.
//
@Suppress("MemberVisibilityCanBePrivate")
class PooledObject<T : Any>(val target: T, private val pool: ObjectPool<T>) : Closeable {

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
        System.identityHashCode(target)
    }

    override fun close() {
        if (status == PooledObjectStatus.Using) {
            status = PooledObjectStatus.Returning
            pool.returnObject(this)
        }
    }

    override fun toString(): String {
        val buf = StringBuilder()
        buf.appendLine("PooledObject:")
        buf.appendLine("  identityHashCode: $identityHashCode")
        buf.appendLine("  target type: ${target.javaClass.name}")
        buf.appendLine("  status: ${status.name}")
        buf.appendLine("  createTime: ${epochMsToLocalDateTime(createTimeMs)}")
        buf.appendLine("  lastBorrowTime: ${epochMsToLocalDateTime(lastBorrowTimeMs)}")
        buf.appendLine("  lastReturnTime: ${epochMsToLocalDateTime(lastReturnTimeMs)}")
        buf.appendLine("  isBroken: $broken")

        return buf.toString()
    }
}

enum class PooledObjectStatus {
    Idle,
    Using,
    Returning,
    Broken,
}