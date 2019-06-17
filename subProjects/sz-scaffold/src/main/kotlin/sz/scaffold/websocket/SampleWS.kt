package sz.scaffold.websocket

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import jodd.datetime.JDateTime
import sz.scaffold.Application
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019-06-17.
//
class SampleWS : WebSocketHandler {
    override fun handle(webSocket: ServerWebSocket) {
        val consumer = Application.vertx.eventBus().consumer<String>(eventBusAddress) {
            Logger.debug("输出事件消息:${it.body()}")
            webSocket.writeTextMessage(it.body())
        }.exceptionHandler {
            Logger.warn("输出事件消息异常:$it")
        }

        val timerID = Application.vertx.setPeriodic(30000) { _ ->
            try {
                Logger.debug("[Timer] send ping to client to keep connection.")
                webSocket.writePing(Buffer.buffer("PING"))
            } catch (ex: Throwable) {
                Logger.debug("定时器调用里出现异常:\n${ex.message}")
            }
        }

        webSocket.closeHandler {
            // Set a close handler. This will be called when the WebSocket is closed.
            Logger.debug("[${JDateTime()}] WebSocket is closed")
            consumer.unregister()
            Application.vertx.cancelTimer(timerID)
            Logger.debug("[${JDateTime()}] consumer is unregister")
        }.exceptionHandler {
            // Set an exception handler on the read stream.
            Logger.warn("[${JDateTime()}] WebSocket 有异常发生:\n$it")
        }.handler {
            Logger.debug("[${JDateTime()}] WebSocket 接收到client 发过来的消息:\n${it.toString(Charsets.UTF_8)}", AnsiColor.YELLOW)
        }.pongHandler {
            Logger.debug("[${JDateTime()}] websocket pong handler:\n${it.toString(Charsets.UTF_8)}")
        }

        Logger.debug("[${JDateTime()}] 收到 client 端的 web socket 请求, accept it")
        webSocket.accept()
    }

    companion object {
        val eventBusAddress = "websocket.SampleWS"

        fun publishMsgToAllClients(msg: String) {
            Application.vertx.eventBus().publish(eventBusAddress, msg)
        }
    }
}