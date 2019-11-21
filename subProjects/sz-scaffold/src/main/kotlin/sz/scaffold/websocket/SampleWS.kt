package sz.scaffold.websocket

import io.vertx.core.http.ServerWebSocket
import jodd.datetime.JDateTime
import sz.scaffold.Application
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger

/**
 * 注: 该类的实现必须是线程安全的.
 *     fun handle(webSocket: ServerWebSocket) 方法会被多个线程同时调用
 */
class SampleWS : WebSocketHandler {
    override fun handle(webSocket: ServerWebSocket) {
        val consumer = Application.vertx.eventBus().consumer<String>(eventBusAddress) {
            webSocket.writeTextMessage(it.body())
        }.exceptionHandler {
            Logger.warn(it.toString())
        }

        webSocket.closeHandler {
            // Set a close handler. This will be called when the WebSocket is closed.
            Logger.debug("[${JDateTime()}] WebSocket is closed")
            consumer.unregister()
            Logger.debug("[${JDateTime()}] consumer is unregister")
        }.exceptionHandler {
            // Set an exception handler on the read stream.
            Logger.warn("[${JDateTime()}] WebSocket 有异常发生:\n$it")
        }.textMessageHandler {
            Logger.debug("[${JDateTime()}] WebSocket 接收到client 发过来的消息:\n$it", AnsiColor.YELLOW)
        }.pongHandler {
            Logger.debug("[${JDateTime()}] websocket pong handler:\n${it.toString(Charsets.UTF_8)}")
        }

        Logger.debug("[${JDateTime()}] 收到 client 端的 web socket 请求, accept it")
        webSocket.accept()
    }

    companion object {
        const val eventBusAddress = "websocket.SampleWS"

        fun publishMsgToAllClients(msg: String) {
            Application.vertx.eventBus().publish(eventBusAddress, msg)
        }
    }
}