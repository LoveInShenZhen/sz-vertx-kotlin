package sz.scaffold.websocket

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.core.http.WebSocketFrame
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019-06-17.
//
class AutoPingServerWebSocket(private val delegate: ServerWebSocket, private val vertx: Vertx, private val pingInterval: Long) : ServerWebSocket by delegate {

    private var timer: Long? = null

    private fun stopTimer() {
        timer?.let {
//            Logger.debug("Stop ping timer: $timer")
            vertx.cancelTimer(it)
            timer = null
        }
    }

    override fun accept() {
        timer = vertx.setPeriodic(pingInterval) {
//            Logger.debug("[Timer] send ping to client to keep connection.")
            delegate.writePing(Buffer.buffer("PING"))
        }
//        Logger.debug("Start ping timer: $timer")
        delegate.accept()
    }

    override fun close() {
        stopTimer()
        delegate.close()
    }

    override fun endHandler(endHandler: Handler<Void>): ServerWebSocket {
        val newHandler = Handler<Void> { event ->
            stopTimer()
            endHandler.handle(event)
        }
        return delegate.endHandler(newHandler)
    }

    override fun closeHandler(handler: Handler<Void>): ServerWebSocket {
        val newHandler = Handler<Void> { event ->
            stopTimer()
            handler.handle(event)
        }
        return delegate.closeHandler(newHandler)
    }
}