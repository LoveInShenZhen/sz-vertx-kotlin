package sz.scaffold.websocket

import io.vertx.core.http.ServerWebSocket

//
// Created by kk on 2019-06-17.
//
class RejectHandler : WebSocketHandler {
    override fun handle(webSocket: ServerWebSocket) {
        webSocket.reject(404)
    }
}