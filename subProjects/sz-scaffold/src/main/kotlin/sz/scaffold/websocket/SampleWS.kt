package sz.scaffold.websocket

import io.vertx.core.http.ServerWebSocket
import sz.scaffold.log
import sz.scaffold.Application
import java.time.LocalDateTime

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
            log.debug("[${LocalDateTime.now()}] WebSocket is closed")
            consumer.unregister()
            log.debug("[${LocalDateTime.now()}] consumer is unregister")
        }.exceptionHandler {
            // Set an exception handler on the read stream.
            log.warn("[${LocalDateTime.now()}] WebSocket 有异常发生:\n$it")
        }.textMessageHandler {
            log.debug("[${LocalDateTime.now()}] WebSocket 接收到client 发过来的消息:\n$it")
        }.pongHandler {
            log.debug("[{}] websocket pong handler:\n{}", LocalDateTime.now(), it.toString(Charsets.UTF_8))
        }

        log.debug("[${LocalDateTime.now()}] 收到 client 端的 web socket 请求, accept it")
    }

    companion object {
        const val eventBusAddress = "websocket.SampleWS"

        fun publishMsgToAllClients(msg: String) {
            Application.vertx.eventBus().publish(eventBusAddress, msg)
        }
    }
}