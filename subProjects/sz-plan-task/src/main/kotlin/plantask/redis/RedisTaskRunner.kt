package plantask.redis

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import sz.scaffold.ext.ChainToString
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//
// Created by kk on 2017/9/28.
//
class RedisTaskRunner : AbstractVerticle() {

    private var consumer: MessageConsumer<String>? = null
    private var checkerTimerId: Long = -1
    private val seqTaskWorker: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    override fun start() {
        consumer = this.vertx.eventBus().consumer<String>(address) { taskKey ->
            try {
                val recordKey = taskKey.body()
                val taskJson = RedisPlanTask.jedis().use { jedis -> jedis.get(recordKey) }
                val redisTask = RedisTask.parse(taskJson)
                if (redisTask == null) {
                    Logger.error("RedisTask, record key: $recordKey can not parse to task class. value:\n${taskJson}")

                    RedisPlanTask.jedis().use { jedis ->
                        val taskTran = jedis.multi()
                        taskTran.srem(RedisPlanTask.processingQueueKey, recordKey)
                        taskTran.sadd(RedisPlanTask.errorQueueKey, recordKey)
                        taskTran.exec()
                    }
                    return@consumer
                }

                val now = JDateTime()
                val delay = redisTask.delayInMs(now)
                if (delay > 0) {
                    Logger.debug("定时器添加任务, delay $delay ms")
                    if (redisTask.ordered) {
                        // 进入有序单线程队列
                        this.seqTaskWorker.schedule(wrapperRunTask(redisTask), delay, TimeUnit.MICROSECONDS)
                    } else {
                        // 对顺序无要求
                        this.vertx.setTimer(delay) { timerId ->
                            try {
                                wrapperRunTask(redisTask).run()
                            } finally {
                                this.vertx.cancelTimer(timerId)
                            }
                        }
                    }

                } else {
                    // delay == 0  立即执行
                    Logger.debug("立即执行任务")
                    if (redisTask.ordered) {
                        // 进入有序单线程队列
                        this.seqTaskWorker.submit(wrapperRunTask(redisTask))
                    } else {
                        // 对顺序无要求
                        this.vertx.executeBlocking<String>({ future ->
                            redisTask.run()
                            future.complete(redisTask.recordKey())
                        },
                                false,
                                { result ->
                                    if (result.succeeded()) {
                                        // 执行成功
                                        RedisPlanTask.jedis().use { jedis ->
                                            val taskTran = jedis.multi()
                                            // 从执行队列里删除任务索引key
                                            taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                                            if (redisTask.singleton.not()) {
                                                // 普通一次性任务(非单例类型任务), 执行完后, 需要把任务数据本身删掉
                                                taskTran.del(redisTask.recordKey())
                                            }
                                            // 单例任务的数据, 保留不删除. 通常, 单例任务的任务json数据是会被本身更新的
                                            taskTran.exec()
                                        }
                                    } else {
                                        // task 执行发生异常, 将 异常任务 记录并转移到 errorQueue
                                        RedisPlanTask.jedis().use { jedis ->
                                            val taskTran = jedis.multi()
                                            taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                                            redisTask.error = ExceptionUtil.exceptionChainToString(result.cause())
                                            taskTran.set(redisTask.recordKey(), redisTask.toJsonPretty())
                                            taskTran.sadd(RedisPlanTask.errorQueueKey, redisTask.recordKey())
                                            taskTran.exec()

                                            Logger.warn(redisTask.error)
                                        }
                                    }
                                })
                    }

                }
            } catch (ex: Exception) {
                Logger.error(ex.ChainToString())
            }
        }

        Logger.debug("RedisTaskRunner started.")
    }

    override fun stop() {
        if (consumer != null) {
            consumer!!.unregister()
        }

        if (checkerTimerId > -1) {
            this.vertx.cancelTimer(checkerTimerId)
        }

        this.seqTaskWorker.shutdown()
        this.seqTaskWorker.awaitTermination(10, TimeUnit.SECONDS)

        Logger.debug("RedisTaskRunner stopped.")
    }

    fun wrapperRunTask(redisTask: RedisTask): Runnable {
        return Runnable {
            try {
                redisTask.run()
                // 执行成功
                RedisPlanTask.jedis().use { jedis ->
                    val taskTran = jedis.multi()
                    // 从执行队列里删除任务索引key
                    taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                    if (redisTask.singleton.not()) {
                        // 普通一次性任务(非单例类型任务), 执行完后, 需要把任务数据本身删掉
                        taskTran.del(redisTask.recordKey())
                    }
                    // 单例任务的数据, 保留不删除. 通常, 单例任务的任务json数据是会被本身更新的
                    taskTran.exec()
                }
            } catch (ex: Exception) {
                // task 执行发生异常, 将 异常任务 记录并转移到 errorQueue
                Logger.debug(ex.ChainToString())
                RedisPlanTask.jedis().use { jedis ->
                    val taskTran = jedis.multi()
                    taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                    redisTask.error = ex.ChainToString()
                    taskTran.set(redisTask.recordKey(), redisTask.toJsonPretty())
                    taskTran.sadd(RedisPlanTask.errorQueueKey, redisTask.recordKey())
                    taskTran.exec()

                    Logger.warn(redisTask.error)
                }
            }
        }
    }

    companion object {
        val address = "sz.app.plantask.redis.runTask"
        private var deoloyId = ""
        private var vertxRef: Vertx? = null

        fun notifyRunTask(taskKey: String) {
            if (vertxRef == null) {
                throw SzException("RedisTaskRunner not deployed.")
            }
            vertxRef!!.eventBus().publish(address, taskKey)
        }

        fun deploy(vertx: Vertx) {
            val options = DeploymentOptions()
            options.isWorker = true
            val verticle = RedisTaskRunner()
            vertx.deployVerticle(verticle, options) { res ->
                if (res.succeeded()) {
                    deoloyId = res.result()
                    vertxRef = vertx
                } else {
                    Logger.error("Deploy TaskLoader verticle failed.")
                    vertx.close()
                }
            }
        }

        fun unDeploy() {
            if (deoloyId.isNotBlank()) {
                vertxRef!!.undeploy(deoloyId)
            }
        }
    }
}