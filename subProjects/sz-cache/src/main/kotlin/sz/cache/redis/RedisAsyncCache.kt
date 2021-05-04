package sz.cache.redis

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.redis.client.getAwait
import io.vertx.redis.client.Command
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.Request
import sz.cache.AsyncCacheApi
import sz.redis.api
import sz.redis.psetexAwait
import sz.redis.setAwait
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019/11/12.
//

class RedisAsyncCache(private val redis: Redis) : AsyncCacheApi {

    private val redisApi: RedisAPI = redis.api()

    override fun exists(key: String): Future<Boolean> {
        val result = Promise.promise<Boolean>()
        redisApi.exists(listOf(key)).onSuccess { response ->
            result.complete(response.toInteger() == 1)
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun del(key: String): Future<Unit> {
        val result = Promise.promise<Unit>()
        redisApi.del(listOf(key)).onSuccess {
            result.complete()
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun getBytes(key: String): Future<ByteArray> {
        val result = Promise.promise<ByteArray>()
        redisApi.get(key).onSuccess { response ->
            if (response != null) {
                result.complete(response.toBytes())
            } else {
                result.fail(SzException("'$key' does not exist in the cache."))
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun getBytesOrElse(key: String, default: () -> ByteArray): Future<ByteArray> {
        val result = Promise.promise<ByteArray>()
        redisApi.get(key).onSuccess { response ->
            if (response != null) {
                result.complete(response.toBytes())
            } else {
                result.complete(default())
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun getBytesOrNull(key: String): Future<ByteArray?> {
        val result = Promise.promise<ByteArray?>()
        redisApi.get(key).onSuccess { response ->
            if (response != null) {
                result.complete(response.toBytes())
            } else {
                result.complete(null)
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun setBytes(key: String, value: ByteArray): Future<Unit> {
        val result = Promise.promise<Unit>()
        val req = Request.cmd(Command.SET).arg(key).arg(value)
        redis.send(req).onSuccess {
            result.complete()
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long): Future<Unit> {
        val result = Promise.promise<Unit>()
        if (expirationInMs > 0) {
            val req = Request.cmd(Command.PSETEX).arg(key).arg(expirationInMs).arg(value)
            redis.send(req).onSuccess {
                result.complete()
            }.onFailure { ex ->
                result.fail(ex)
            }
        }

        return result.future()
    }


}