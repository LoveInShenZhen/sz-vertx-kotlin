package sz.scaffold.websocket

import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.reply.ReplyBase

//
// Created by kk on 2019-06-17.
//

@Comment("SampleWS 对应的测试程序")
class SampleWSController : ApiController() {

    @Comment("广播消息给所有连接中的webSocket客户端")
    suspend fun publishMsgToWebSocketClients(@Comment("消息内容") msg: String): ReplyBase {
        val reply = ReplyBase()
        SampleWS.publishMsgToAllClients(msg)
        return reply
    }

}