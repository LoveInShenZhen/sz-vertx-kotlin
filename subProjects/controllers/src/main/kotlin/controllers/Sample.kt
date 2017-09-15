package controllers

import sz.scaffold.annotations.Comment
import sz.scaffold.cache.CacheApi
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.reply.ReplyBase
import tasks.SampleTask


@Comment("样例/测试")
class Sample : ApiController() {

    @Comment("KK 临时测试代码")
    fun kktest(@Comment("保存到redis的消息") msg: String): ReplyBase {
        val reply = ReplyBase()

        val task = SampleTask()

        CacheApi.redisCache.set("kktest", msg)

        return reply
    }
}