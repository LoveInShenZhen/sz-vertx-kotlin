package plantask.redis

import jodd.datetime.JDateTime
import redis.clients.jedis.Jedis
import sz.scaffold.Application
import sz.scaffold.cache.redis.JRedisPool
import sz.scaffold.ext.ChainToString
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2017/9/27.
//
object RedisPlanTask {

    val justRunTime = JDateTime("2017-09-11 12:00:00", "YYYY-MM-DD hh:mm:ss")
    val taskIdKey = "Id:PlanTask"
    val waitingQueueKey = "PlanTask:WaitingQueue"           // 有序Set, 元素为: recordKey
    val processingQueueKey = "PlanTask:ProcessingQueue"     // Set, 元素为: recordKey
    val errorQueueKey = "PlanTask:ErrorQueue"               // Set, 元素为: recordKey

    fun jedis(configName: String = "planTask"): Jedis {
//        Logger.debug("=".repeat(64))
//        Logger.debug("numActive: ${JRedisPool.byName(configName).pool().numActive}                      ")
//        Logger.debug("numIdle: ${JRedisPool.byName(configName).pool().numIdle}                          ")
//        Logger.debug("numWaiters: ${JRedisPool.byName(configName).pool().numWaiters}                    ")
//        Logger.debug("=".repeat(64))
        return JRedisPool.byName(configName).jedis()
    }

    private fun nextId(): Long {
        return jedis().use { jedis -> jedis.incr(taskIdKey).toLong() }
    }

    fun addTask(task: Runnable, planRunTime: JDateTime = justRunTime, tag: String = "") {
        try {
            val redisTask = RedisTask()
            redisTask.id = nextId()
            redisTask.planRunTime = planRunTime
            redisTask.className = task::class.java.name
            redisTask.jsonData = task.toJsonPretty()
            redisTask.tag = tag

            jedis().use { jedis ->
                Logger.debug("go in to addTask tran")
                val tran = jedis.multi()

                tran.set(redisTask.recordKey(), redisTask.toJsonPretty())

                tran.zadd(waitingQueueKey, redisTask.score(), redisTask.recordKey())

                tran.exec()
                Logger.debug("addTask tran.exec()")
            }

            notifyNewTask()

        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            throw SzException(msg = "Failed to add RedisPlanTask", cause = ex)
        }
    }

    fun notifyNewTask() {
        Application.vertx.eventBus().send(RedisTaskLoader.address, "")
    }
}

fun RedisTask.recordKey(): String {
    return "Record:PlanTask:${this.id}"
}
