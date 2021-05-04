package sz.cache.redis

import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.Redis
import kotlinx.coroutines.runBlocking
import sz.cache.CacheApi


class RedisCache(redis: Redis) : CacheApi {

    private val delegate = RedisAsyncCache(redis)

    override fun exists(key: String): Boolean = runBlocking {
        delegate.exists(key).await()
    }

    override fun del(key: String) = runBlocking {
        delegate.del(key).await()
    }

    override fun getBytes(key: String): ByteArray = runBlocking {
        delegate.getBytes(key).await()
    }

    override fun getBytesOrElse(key: String, default: () -> ByteArray): ByteArray = runBlocking {
        delegate.getBytesOrElse(key, default).await()
    }

    override fun getBytesOrNull(key: String): ByteArray? = runBlocking {
        delegate.getBytesOrNull(key).await()
    }

    override fun setBytes(key: String, value: ByteArray) = runBlocking {
        delegate.setBytes(key, value).await()
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long) = runBlocking {
        delegate.setBytes(key, value, expirationInMs).await()
    }
}