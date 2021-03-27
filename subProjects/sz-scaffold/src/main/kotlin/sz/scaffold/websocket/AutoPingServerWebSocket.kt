package sz.scaffold.websocket

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket

//
// Created by kk on 2019-06-17.
//
class AutoPingServerWebSocket(private val delegate: ServerWebSocket, private val vertx: Vertx, private val pingInterval: Long) : ServerWebSocket by delegate {

    private var timer: Long? = null

    private fun stopTimer() {
        timer?.let {
            vertx.cancelTimer(it)
            timer = null
        }
    }

    override fun accept() {
        timer = vertx.setPeriodic(pingInterval) {
            delegate.writePing(Buffer.buffer("PING"))
        }
        delegate.accept()
    }

    override fun close(): Future<Void> {
        stopTimer()
        return delegate.close()
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