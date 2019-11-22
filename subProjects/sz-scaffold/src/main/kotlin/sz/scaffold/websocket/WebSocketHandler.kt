package sz.scaffold.websocket

import io.netty.handler.codec.http.QueryStringDecoder
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import io.vertx.core.http.ServerWebSocket
import io.vertx.ext.web.handler.impl.HttpStatusException

//
// Created by kk on 2019-06-17.
//

typealias WebSocketHandler = Handler<ServerWebSocket>

fun ServerWebSocket.queryParams(): MultiMap {
    try {
        val decodedParams = QueryStringDecoder(this.uri()).parameters()
        val queryParams = MultiMap.caseInsensitiveMultiMap()
        decodedParams.forEach { (name, values) ->
            queryParams.add(name, values)
        }
        return queryParams
    } catch (ex: IllegalArgumentException) {
        throw HttpStatusException(400, "Error while decoding query params", ex)
    }
}