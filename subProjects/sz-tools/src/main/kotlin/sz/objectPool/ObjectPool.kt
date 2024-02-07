package sz.objectPool

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import sz.logger.log
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.fixedRateTimer

//
// Created by kk on 2019/10/23.
//
@Suppress("MemberVisibilityCanBePrivate")
open class ObjectPool<T : Any>(
    val config: PoolConfig,
    val factory: PooledObjectFactory<T>,
    val name: String = "Unnamed"
) {

    private val idleChannel = Channel<PooledObject<T>>(config.maxTotal)

    private val creatingCounter = AtomicInteger(0)
    private val idleCounter = AtomicInteger(0)
    private val idleObjMap = mutableMapOf<Int, PooledObject<T>>()
    private val usingObjMap = mutableMapOf<Int, PooledObject<T>>()
    private val usingCounter = AtomicInteger(0)
    private val counterMutex = Mutex()

    private var evictionCheckingTimer: Timer? = null

    private val scope = CoroutineScope(Dispatchers.IO)
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
        scope.launch(Dispatchers.IO) {
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
        scope.launch(Dispatchers.IO) {
            try {
                factory.destroyObject(pooledObject.target)
            } catch (ex: Exception) {
                log.warn("${factory.javaClass.name}.onDestoryObject(...) failed.\n$ex")
            }
        }
    }

    fun returnObject(pooledObject: PooledObject<T>) {
        scope.launch {
//            log.debug("return object: $pooledObject")
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
//                    log.debug("==> ${pool.poolInfo()}")
//                    log.debug("==> 执行驱逐策略, 本次驱逐 $count 个")
                    for (i in 1..count) {
                        scope.launch {
                            try {
                                pool.borrowAwait().use {
                                    it.markBroken()
                                }
                            } catch (ex: Exception) {
                                log.warn("Object Pool: [${name}] evictionChecking failed by exception:\n$ex")
                            }

                        }
                    }
                }
            }
        }
    }

    fun poolInfo(): String {
        val sb = StringBuilder()
        sb.appendLine("======== pool config ========")
        sb.appendLine("maxTotal = ${config.maxTotal}")
        sb.appendLine("maxIdle = ${config.maxIdle}")
        sb.appendLine("minIdle = ${config.minIdle}")
        sb.appendLine("======== object count =======")
        val idleCount = idleCounter.get()
        val usingCount = usingCounter.get()
        val creatingCount = creatingCounter.get()
        sb.appendLine("idleCounter = $idleCount")
        sb.appendLine("usingCounter = $usingCount")
        sb.appendLine("creatingCounter = $creatingCount")
        sb.appendLine("total = ${idleCount + usingCount + creatingCount}")
        sb.appendLine("=============================")

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
                factory.destroyObject(it.second.target)
            } catch (ex: Exception) {
                log.warn("${factory.javaClass.name}.onDestoryObject(...) failed.\n$ex")
            }
        }

//        log.debug("using count = ${usingCounter.get()}")
//        log.debug("usingObjMap count = ${usingObjMap.count()}")
//        log.debug("idleObjMap count = ${idleObjMap.count()}")

        idleChannel.close()
        idleCounter.set(0)
        log.debug("This object pool: [${this.name}] was stopped.")

    }

    fun stopBlocking() {
        runBlocking {
            stop()
        }
    }
}