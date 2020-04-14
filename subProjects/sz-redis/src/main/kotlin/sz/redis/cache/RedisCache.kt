package sz.redis.cache

import io.vertx.redis.client.Redis
import kotlinx.coroutines.runBlocking
import sz.scaffold.cache.CacheApi


class RedisCache(redis: Redis) : CacheApi {

    private val delegate = RedisAsyncCache(redis)

    override fun exists(key: String): Boolean = runBlocking {
        delegate.existsAwait(key)
    }

    override fun del(key: String) = runBlocking {
        delegate.delAwait(key)
    }

    override fun getBytes(key: String): ByteArray = runBlocking {
        delegate.getBytesAwait(key)
    }

    override fun getBytesOrElse(key: String, default: () -> ByteArray): ByteArray = runBlocking {
        delegate.getBytesOrElseAwait(key, default)
    }

    override fun getBytesOrNull(key: String): ByteArray? = runBlocking {
        delegate.getBytesOrNullAwait(key)
    }

    override fun setBytes(key: String, value: ByteArray) = runBlocking {
        delegate.setBytesAwait(key, value)
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long) = runBlocking {
        delegate.setBytesAwait(key, value, expirationInMs)
    }
}