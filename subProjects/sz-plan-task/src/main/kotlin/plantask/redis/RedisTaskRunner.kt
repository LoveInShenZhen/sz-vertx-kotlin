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

//
// Created by kk on 2017/9/28.
//
class RedisTaskRunner : AbstractVerticle() {

    private var consumer: MessageConsumer<String>? = null
    private var checkerTimerId: Long = -1
    private var ordered = false

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

                if (redisTask.ordered == this.ordered) {
                    val now = JDateTime()
                    val delay = redisTask.delayInMs(now)
                    if (delay > 0) {
                        Logger.debug("定时器添加任务, delay $delay ms")
                        this.vertx.setTimer(delay) { timerId ->
                            try {
                                redisTask.run()
                                // 执行成功
                                RedisPlanTask.jedis().use { jedis ->
                                    val taskTran = jedis.multi()
                                    taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                                    taskTran.del(redisTask.recordKey())
                                    taskTran.exec()
                                }
                            } catch (ex: Exception) {
                                // task 执行发生异常, 将 异常任务 记录并转移到 errorQueue
                                RedisPlanTask.jedis().use { jedis ->
                                    val taskTran = jedis.multi()
                                    taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                                    redisTask.error = ex.ChainToString()
                                    taskTran.set(redisTask.recordKey(), redisTask.toJsonPretty())
                                    taskTran.sadd(RedisPlanTask.errorQueueKey, redisTask.recordKey())
                                    taskTran.exec()

                                    Logger.warn(redisTask.error)
                                }
                            } finally {
                                this.vertx.cancelTimer(timerId)
                            }
                        }
                    } else {
                        // delay == 0  立即执行
                        Logger.debug("立即执行任务")
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
                                            taskTran.srem(RedisPlanTask.processingQueueKey, redisTask.recordKey())
                                            taskTran.del(redisTask.recordKey())
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

        Logger.debug("RedisTaskRunner stopped.")
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
            options.isMultiThreaded = true
            val verticle = RedisTaskRunner()
            verticle.ordered = false
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

        private var singletonDeployId = ""

        fun deploySingleton(vertx: Vertx) {
            val options = DeploymentOptions()
            options.isWorker = true
            options.isMultiThreaded = false     // 单线程
            options.workerPoolSize = 1
            val verticle = RedisTaskRunner()
            verticle.ordered = true
            vertx.deployVerticle(verticle, options) { res ->
                if (res.succeeded()) {
                    singletonDeployId = res.result()
                    vertxRef = vertx
                } else {
                    Logger.error("Deploy TaskLoader verticle failed.")
                    vertx.close()
                }
            }
        }

        fun unDeploySingleton() {
            if (singletonDeployId.isNotBlank()) {
                vertxRef!!.undeploy(singletonDeployId)
            }
        }
    }
}