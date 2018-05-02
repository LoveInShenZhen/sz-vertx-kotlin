package controllers

import jodd.datetime.JDateTime
import plantask.redis.RedisPlanTask
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.logger.Logger
import tasks.SampleTask


@Comment("样例/测试")
class Sample : ApiController() {

    @Comment("KK 临时测试代码")
    fun kktest(@Comment("保存到redis的消息") msg: String): ReplyBase {
        val reply = ReplyBase()

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