package plantask.redis

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import jodd.datetime.JDateTime
import sz.scaffold.ext.ChainToString
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2017/9/27.
//
class RedisTaskLoader : AbstractVerticle() {

    private var consumer: MessageConsumer<String>? = null
    private var checkerTimerId: Long = -1

    override fun start() {
        resetTaskStatus()

        consumer = this.vertx.eventBus().consumer<String>(address) { _ ->
            try {
                // 收到通知, 去检查是否有新任务可以加入到队列里执行
//                Logger.debug("收到通知, 检查并加载 redis task")
                val scoreMin = 0
                val scoreMax = (JDateTime().addSecond(10).convertToDate().time / 1000).toDouble()

                // 从 WaitingQueue 里找到运行时间在当前时间 + 10秒范围内的task
                // 即找到将来10秒内到达计划运行时间的任务
                val taskRecordKeyList = RedisPlanTask.jedis().use { jedis ->
                    jedis.zrangeByScore(RedisPlanTask.waitingQueueKey, scoreMin.toDouble(), scoreMax)
                }

                taskRecordKeyList.forEach { recordKey ->
                    RedisPlanTask.jedis().use { jedis ->
                        val tran = jedis.multi()
                        // 从 waitingQueue 移除
                        tran.zrem(RedisPlanTask.waitingQueueKey, recordKey)

                        // 添加到 processingQueue
                        tran.sadd(RedisPlanTask.processingQueueKey, recordKey)

                        tran.exec()

                        // 发送消息给 RedisTaskRunner
                        RedisTaskRunner.notifyRunTask(recordKey)
                    }
                }

            } catch (ex: Exception) {
                Logger.error(ex.ChainToString())
            }
        }

        RedisTaskRunner.deploy(this.vertx)

        // 注册一个每隔5秒轮询检查的定时器
        checkerTimerId = this.vertx.setPeriodic(5000) { _ ->
            this.vertx.eventBus().send(address, "")
        }

        // 发送一个消息, 立即检查
        this.vertx.eventBus().send(address, "")

        Logger.debug("RedisTaskLoader started.")
    }

    override fun stop() {
        if (consumer != null) {
            consumer!!.unregister()
        }

        if (checkerTimerId > -1) {
            this.vertx.cancelTimer(checkerTimerId)
        }

        RedisTaskRunner.unDeploy()

        Logger.debug("RedisTaskLoader stopped.")
    }

    companion object {
        val address = "sz.app.plantask.redis.newTask"
        private var deoloyId = ""
        private var vertxRef: Vertx? = null

        fun resetTaskStatus() {
            val lastProcessingTaskKeys = RedisPlanTask.jedis().use { jedis ->
                jedis.smembers(RedisPlanTask.processingQueueKey)
            }

            val lastProcessingTasks = RedisPlanTask.jedis().use { jedis ->
                lastProcessingTaskKeys.map { key ->
                    val jsonStr = jedis.get(key)
                    RedisTask.parse(jsonStr)!!
                }
            }

            RedisPlanTask.jedis().use { jedis ->
                val tran = jedis.multi()
                if (lastProcessingTaskKeys.size > 0) {
                    tran.srem(RedisPlanTask.processingQueueKey, *lastProcessingTaskKeys.toTypedArray())
                    lastProcessingTasks.forEach { redisTask ->
                        tran.zadd(RedisPlanTask.waitingQueueKey, redisTask.score(), redisTask.recordKey())
                    }
                    tran.exec()
                }
            }
        }

        fun deploy(vertx: Vertx) {
            val options = DeploymentOptions()
            options.isWorker = true
            val verticle = RedisTaskLoader()
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