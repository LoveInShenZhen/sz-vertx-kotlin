package com.quantplus.controllers.sample


import com.quantplus.MainApp.Companion.log
import com.quantplus.controllers.sample.reply.HelloReply
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController


@Suppress("DuplicatedCode")
@Comment("测试样例代码")
class SampleController : ApiController() {

    @Comment("测试接口")
    suspend fun hello(@Comment("访问者名称") name: String): HelloReply {
        val reply = HelloReply()

        reply.msg = "Hello $name, 准备就绪, 请开始你的表演!"

        log.debug(reply.msg)

        return reply
    }

}
