package sz.scaffold.websocket

import io.vertx.core.Vertx
import io.vertx.core.http.ServerWebSocket
import jodd.exception.ExceptionUtil
import sz.scaffold.Application
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019-06-17.
//
class WebSocketFilter(private val vertx: Vertx) : WebSocketHandler {

    private val pathHandlerMap = mutableMapOf<String, WebSocketHandler>()
    private val rejectHandler: WebSocketHandler = RejectHandler()

    @Suppress("UNCHECKED_CAST")
    fun addPathAndHandler(path: String, handlerClassName: String) {
        if (pathHandlerMap.containsKey(path)) {
            throw SzException("Duplicate websocket route path: $path")
        }
        val handlerClass = try {
            Application.classLoader.loadClass(handlerClassName)
        } catch (ex: Exception) {
            throw SzException("Can not load websocket handler class by name: $handlerClassName .")
        }
        pathHandlerMap[path] = handlerClass.getDeclaredConstructor().newInstance() as WebSocketHandler
    }

    override fun handle(webSocket: ServerWebSocket) {
        try {
            val handlerInstance = pathHandlerMap.getOrDefault(webSocket.path(), rejectHandler)
            if (autoPingByServer) {
                handlerInstance.handle(AutoPingServerWebSocket(webSocket, vertx, pingInterval))
            } else {
                handlerInstance.handle(webSocket)
            }
        } catch (ex: Exception) {
            webSocket.close(500, ex.message)
            Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
        }
    }

    private val pingInterval: Long
        get() = Application.config.getLong("app.httpServer.webSocket.pingInterval")

    private val autoPingByServer: Boolean
        get() = Application.config.getBoolean("app.httpServer.webSocket.autoPingByServer")

}