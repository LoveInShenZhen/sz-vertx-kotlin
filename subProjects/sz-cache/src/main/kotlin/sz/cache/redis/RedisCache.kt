package sz.cache.redis

import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.redis.client.Redis
import kotlinx.coroutines.runBlocking
import sz.cache.CacheApi


class RedisCache(redis: Redis) : CacheApi {

    private val delegate = RedisAsyncCache(redis)

    override fun exists(key: String): Boolean = runBlocking {
        delegate.exists(key).coAwait()
    }

    override fun del(key: String) = runBlocking {
        delegate.del(key).coAwait()
    }

    override fun getBytes(key: String): ByteArray = runBlocking {
        delegate.getBytes(key).coAwait()
    }

    override fun getBytesOrElse(key: String, default: () -> ByteArray): ByteArray = runBlocking {
        delegate.getBytesOrElse(key, default).coAwait()
    }

    override fun getBytesOrNull(key: String): ByteArray? = runBlocking {
        delegate.getBytesOrNull(key).coAwait()
    }

    override fun setBytes(key: String, value: ByteArray) = runBlocking {
        delegate.setBytes(key, value).coAwait()
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long) = runBlocking {
        delegate.setBytes(key, value, expirationInMs).coAwait()
    }
}