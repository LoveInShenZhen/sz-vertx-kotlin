package sz.scaffold.websocket

import io.vertx.core.http.ServerWebSocket
import jodd.datetime.JDateTime
import sz.logger.log
import sz.scaffold.Application

/**
 * 注: 该类的实现必须是线程安全的.
 *     fun handle(webSocket: ServerWebSocket) 方法会被多个线程同时调用
 */
class SampleWS : WebSocketHandler {
    override fun handle(webSocket: ServerWebSocket) {
        val consumer = Application.vertx.eventBus().consumer<String>(eventBusAddress) {
            webSocket.writeTextMessage(it.body())
        }.exceptionHandler {
            log.warn(it.toString())
        }

        webSocket.closeHandler {
            // Set a close handler. This will be called when the WebSocket is closed.
            log.debug("[${JDateTime()}] WebSocket is closed")
            consumer.unregister()
            log.debug("[${JDateTime()}] consumer is unregister")
        }.exceptionHandler {
            // Set an exception handler on the read stream.
            log.warn("[${JDateTime()}] WebSocket 有异常发生:\n$it")
        }.textMessageHandler {
            log.debug("[${JDateTime()}] WebSocket 接收到client 发过来的消息:\n$it")
        }.pongHandler {
            log.debug("[${JDateTime()}] websocket pong handler:\n${it.toString(Charsets.UTF_8)}")
        }

        log.debug("[${JDateTime()}] 收到 client 端的 web socket 请求, accept it")
        webSocket.accept()
    }

    companion object {
        const val eventBusAddress = "websocket.SampleWS"

        fun publishMsgToAllClients(msg: String) {
            Application.vertx.eventBus().publish(eventBusAddress, msg)
        }
    }
}