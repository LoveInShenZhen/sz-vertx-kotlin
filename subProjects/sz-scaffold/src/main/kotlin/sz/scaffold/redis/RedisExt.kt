package sz.scaffold.redis

import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.redis.client.sendAwait
import io.vertx.redis.client.*

//
// Created by kk on 2020/4/13.
//

suspend fun Redis.setAwait(key: String, value: ByteArray): Response? {
    val req = Request.cmd(Command.SET).arg(key).arg(value)
    return this.send(req).await()
}

suspend fun Redis.psetexAwait(key: String, value: ByteArray, expirationInMs: Long): Response? {
    val req = Request.cmd(Command.PSETEX).arg(key).arg(expirationInMs).arg(value)
    return this.send(req).await()
}

fun Redis.api(): RedisAPI {
    return RedisAPI.api(this)
}