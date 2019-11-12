package sz.objectPool

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sz.scaffold.tools.logger.Logger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.fixedRateTimer

//
// Created by kk on 2019/10/23.
//
@Suppress("MemberVisibilityCanBePrivate")
open class ObjectPool<T : Any>(val config: PoolConfig, val factory: PooledObjectFactory<T>, val name: String = "Unnamed") {

    private val idleChannel = Channel<PooledObject<T>>(config.maxTotal)

    private val creatingCounter = AtomicInteger(0)
    private val idleCounter = AtomicInteger(0)
    private val idleObjMap = mutableMapOf<Int, PooledObject<T>>()
    private val usingObjMap = mutableMapOf<Int, PooledObject<T>>()
    private val usingCounter = AtomicInteger(0)
    private val counterMutex = Mutex()

    private var evictionCheckingTimer: Timer? = null

    private var stopped = false

    init {
        evictionChecking()
    }

    private suspend fun borrowObjectAwait(): PooledObject<T> {
        if (stopped) {
            throw RuntimeException("ObjectPool: ${this.javaClass.name} is stopped. Now, user can not borrow object from it.")
        }

        if (idleCounter.get() == 0) {
            tryCreate()
        }

        val pooledObj = idleChannel.receive()

        counterMutex.withLock {
            idleObjMap.remove(pooledObj.identityHashCode)
            idleCounter.decrementAndGet()

            usingObjMap[pooledObj.identityHashCode] = pooledObj
            usingCounter.incrementAndGet()
        }

        pooledObj.lastBorrowTimeMs = System.currentTimeMillis()
        pooledObj.status = PooledObjectStatus.Using
        return pooledObj
    }

    suspend fun borrowAwait(timeOutMs: Long = config.borrowTimeoutMs): PooledObject<T> {
        return if (timeOutMs > 0) {
            withTimeout(timeOutMs) {
                borrowObjectAwait()
            }
        } else {
            borrowObjectAwait()
        }

    }

    fun borrowBlocking(timeOutMs: Long = config.borrowTimeoutMs): PooledObject<T> {
        return runBlocking {
            if (timeOutMs > 0) {
                withTimeout(timeOutMs) {
                    borrowObjectAwait()
                }
            } else {
                borrowObjectAwait()
            }
        }
    }

    private suspend fun tryCreate() {
        counterMutex.withLock {
            if (idleCounter.get() + creatingCounter.get() + usingCounter.get() < config.maxTotal) {
                // 需要创建
                creatingCounter.incrementAndGet()
                doCreate()
            }
        }
    }

    private suspend fun doCreate() {
        GlobalScope.launch(Dispatchers.IO) {
            val obj = factory.wrapObject(this@ObjectPool)
            if (obj != null) {
                idleChannel.send(obj)

                counterMutex.withLock {
                    idleObjMap[obj.identityHashCode] = obj
                    creatingCounter.decrementAndGet()
                    idleCounter.incrementAndGet()
                }
            }

        }
    }

    private fun doDestory(pooledObject: PooledObject<T>) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                factory.destoryObject(pooledObject.target)
            } catch (ex: Exception) {
                Logger.warn("${factory.javaClass.name}.onDestoryObject(...) failed.\n$ex")
            }
        }
    }

    fun returnObject(pooledObject: PooledObject<T>) {
        GlobalScope.launch {
            Logger.debug("return object: $pooledObject")
            if (pooledObject.broken) {
                pooledObject.status = PooledObjectStatus.Broken
                counterMutex.withLock {
                    usingObjMap.remove(pooledObject.identityHashCode)
                    usingCounter.decrementAndGet()
                }
                doDestory(pooledObject)
            } else {
                counterMutex.withLock {
                    usingObjMap.remove(pooledObject.identityHashCode)
                    usingCounter.decrementAndGet()
                }

                pooledObject.status = PooledObjectStatus.Idle
                pooledObject.lastReturnTimeMs = System.currentTimeMillis()
                idleChannel.send(pooledObject)

                counterMutex.withLock {
                    idleObjMap[pooledObject.identityHashCode] = pooledObject
                    idleCounter.incrementAndGet()
                }
            }
        }

    }

    private fun evictionChecking() {
        if (config.maxIdle > 0 && config.maxIdle < config.maxTotal) {
            val pool = this
            evictionCheckingTimer = fixedRateTimer(
                initialDelay = (config.timeBetweenEvictionRunsSeconds * 1000).toLong(),
                period = (config.timeBetweenEvictionRunsSeconds * 1000).toLong()
            ) {
                val exceed = pool.idleCounter.get() - pool.config.maxIdle
                if (exceed > 0) {
                    val count = if (pool.config.minIdle > 0) {
                        pool.idleCounter.get() - pool.config.minIdle
                    } else {
                        exceed
                    }
//                    Logger.debug("==> ${pool.poolInfo()}")
//                    Logger.debug("==> 执行驱逐策略, 本次驱逐 $count 个")
                    for (i in 1..count) {
                        GlobalScope.launch {
                            try {
                                pool.borrowAwait().use {
                                    it.markBroken()
                                }
                            } catch (ex: Exception) {
                                Logger.warn("Object Pool: [${name}] evictionChecking failed by exception:\n$ex")
                            }

                        }
                    }
                }
            }
        }
    }

    fun poolInfo(): String {
        val sb = StringBuilder()
        sb.appendln("======== pool config ========")
        sb.appendln("maxTotal = ${config.maxTotal}")
        sb.appendln("maxIdle = ${config.maxIdle}")
        sb.appendln("minIdle = ${config.minIdle}")
        sb.appendln("======== object count =======")
        val idleCount = idleCounter.get()
        val usingCount = usingCounter.get()
        val creatingCount = creatingCounter.get()
        sb.appendln("idleCounter = $idleCount")
        sb.appendln("usingCounter = $usingCount")
        sb.appendln("creatingCounter = $creatingCount")
        sb.appendln("total = ${idleCount + usingCount + creatingCount}")
        sb.appendln("=============================")

        return sb.toString()
    }

    suspend fun stop() {
        if (stopped) return

        stopped = true
        evictionCheckingTimer?.cancel()

        while (counterMutex.withLock { usingCounter.get() > 0 || creatingCounter.get() > 0 }) {
            delay(200)
        }

        idleObjMap.toList().forEach {
            try {
                idleObjMap.remove(it.first)
                factory.destoryObject(it.second.target)
            } catch (ex: Exception) {
                Logger.warn("${factory.javaClass.name}.onDestoryObject(...) failed.\n$ex")
            }
        }

//        Logger.debug("using count = ${usingCounter.get()}")
//        Logger.debug("usingObjMap count = ${usingObjMap.count()}")
//        Logger.debug("idleObjMap count = ${idleObjMap.count()}")

        idleChannel.close()
        idleCounter.set(0)
        Logger.debug("This object pool: [${this.name}] was stopped.")

    }

    fun stopBlocking() {
        runBlocking {
            stop()
        }
    }
}