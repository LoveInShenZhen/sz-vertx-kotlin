package sz.objectPool

import java.io.Closeable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

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
        buf.appendLine("  createTime: ${createTimeMs.toLocalDateTime()}")
        buf.appendLine("  lastBorrowTime: ${lastBorrowTimeMs.toLocalDateTime()}")
        buf.appendLine("  lastReturnTime: ${lastReturnTimeMs.toLocalDateTime()}")
        buf.appendLine("  isBroken: $broken")

        return buf.toString()
    }
}

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        ZoneId.systemDefault()
    )
}

enum class PooledObjectStatus {
    Idle,
    Using,
    Returning,
    Broken,
}