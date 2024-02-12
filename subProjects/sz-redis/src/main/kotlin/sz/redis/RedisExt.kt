package sz.redis

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.redis.client.*

//
// Created by kk on 2020/4/13.
//

suspend fun Redis.setAwait(key: String, value: ByteArray): Response? {
    val req = Request.cmd(Command.SET).arg(key).arg(value)
    return this.send(req).coAwait()
}

suspend fun Redis.psetexAwait(key: String, value: ByteArray, expirationInMs: Long): Response? {
    val req = Request.cmd(Command.PSETEX).arg(key).arg(expirationInMs).arg(value)
    return this.send(req).coAwait()
}

fun Redis.api(): RedisAPI {
    return RedisAPI.api(this)
}