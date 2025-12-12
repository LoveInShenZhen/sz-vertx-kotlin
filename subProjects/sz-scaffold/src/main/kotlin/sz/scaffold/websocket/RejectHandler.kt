package sz.scaffold.websocket

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket

//
// Created by kk on 2019-06-17.
//
class RejectHandler : WebSocketHandler {
    override fun handle(webSocket: ServerWebSocket) {
        webSocket.close(404, "未找到 http请求Path 对应的 WebSocket 处理器")

        webSocket.writePing(Buffer.buffer("PING"))
    }
}