package controllers

import io.vertx.ext.web.Cookie
import jodd.datetime.JDateTime
import models.PlanTask
import plantask.redis.RedisPlanTask
import plantask.redis.RedisTask
import plantask.redis.recordKey
import sz.AsynTask.AsyncTask
import sz.DB
import sz.scaffold.annotations.Comment
import sz.scaffold.cache.CacheApi
import sz.scaffold.cache.redis.JRedisPool
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import tasks.SampleTask


@Comment("样例/测试")
class Sample : ApiController() {

    @Comment("KK 临时测试代码")
    fun kktest(@Comment("保存到redis的消息") msg: String): ReplyBase {
        val reply = ReplyBase()

        val task = SampleTask()

        CacheApi.redisCache.set("kktest", msg)

        this.httpContext.removeCookie("kktest")
        this.httpContext.addCookie(Cookie.cookie("kktest", msg))

        return reply
    }

    @Comment("测试EventBus")
    fun testSendEventBus() : ReplyBase {
        Logger.debug("testSendEventBus begin")
        val task = SampleTask()
        RedisPlanTask.addTask(task, JDateTime().addSecond(9))
        RedisPlanTask.addTask(task, JDateTime().addSecond(9))
        RedisPlanTask.addTask(task, JDateTime().addSecond(9))
        RedisPlanTask.addTask(task, JDateTime().addSecond(9))

        return ReplyBase()
    }
}