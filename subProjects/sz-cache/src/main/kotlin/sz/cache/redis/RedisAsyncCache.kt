package sz.cache.redis

import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.redis.client.getAwait
import io.vertx.redis.client.Redis
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

    override suspend fun existsAwait(key: String): Boolean {
        return try {
            redis.api().exists(listOf(key)).await().toInteger() == 1
        } catch (e: Throwable) {
            false
        }
    }

    override suspend fun delAwait(key: String) {
        try {
            redis.api().del(listOf(key)).await()
        } catch (e: Throwable) {
            Logger.warn(e.localizedMessage)
        }
    }

    override suspend fun getBytesAwait(key: String): ByteArray {
        return redis.api().get(key).await().toBytes() ?: throw SzException("'$key' does not exist in the cache.")
    }

    override suspend fun getBytesOrElseAwait(key: String, default: () -> ByteArray): ByteArray {
        return try {
            redis.api().get(key).await().toBytes() ?: default()
        } catch (e: Throwable) {
            Logger.warn(e.localizedMessage)
            default()
        }
    }

    override suspend fun getBytesOrNullAwait(key: String): ByteArray? {
        return try {
            redis.api().getAwait(key)?.toBytes()
        } catch (e: Throwable) {
            Logger.warn(e.localizedMessage)
            null
        }
    }

    override suspend fun setBytesAwait(key: String, value: ByteArray) {
        try {
            redis.setAwait(key, value)
        } catch (e: Throwable) {
            Logger.warn(e.localizedMessage)
        }
    }

    override suspend fun setBytesAwait(key: String, value: ByteArray, expirationInMs: Long) {
        try {
            if (expirationInMs > 0) {
                redis.psetexAwait(key, value, expirationInMs)
            } else {
                redis.setAwait(key, value)
            }
        } catch (e: Throwable) {
            Logger.warn(e.localizedMessage)
        }
    }


}