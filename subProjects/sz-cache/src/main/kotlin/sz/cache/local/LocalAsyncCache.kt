package sz.cache.local

import com.google.common.cache.Cache
import io.vertx.core.Future
import io.vertx.core.Promise
import sz.cache.AsyncCacheApi
import sz.scaffold.tools.SzException

//
// Created by kk on 2021/5/4.
//
class LocalAsyncCache(private val cacheImp: Cache<String, CacheEntry>) : AsyncCacheApi {
    override fun exists(key: String): Future<Boolean> {
        val result = Promise.promise<Boolean>()
        val entry = cacheImp.getIfPresent(key)
        when {
            entry == null -> {
                result.complete(false)
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                result.complete(false)
            }
            else -> {
                result.complete(true)
            }
        }
        return result.future()
    }

    override fun del(key: String): Future<Unit> {
        val result = Promise.promise<Unit>()
        cacheImp.invalidate(key)
        result.complete()
        return result.future()
    }

    override fun getBytes(key: String): Future<ByteArray> {
        val result = Promise.promise<ByteArray>()
        val entry = cacheImp.getIfPresent(key)
        when {
            entry == null -> {
                result.fail(SzException("'$key' does not exist in the cache."))
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                result.fail(SzException("'$key' does not exist in the cache."))
            }
            else -> {
                result.complete(entry.content)
            }
        }
        return result.future()
    }

    override fun getBytesOrElse(key: String, default: () -> ByteArray): Future<ByteArray> {
        val result = Promise.promise<ByteArray>()
        val entry = cacheImp.getIfPresent(key)
        when {
            entry == null -> {
                result.complete(default())
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                result.complete(default())
            }
            else -> {
                result.complete(entry.content)
            }
        }

        return result.future()
    }

    override fun getBytesOrNull(key: String): Future<ByteArray?> {
        val result = Promise.promise<ByteArray>()
        val entry = cacheImp.getIfPresent(key)
        when {
            entry == null -> {
                result.complete(null)
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                result.complete(null)
            }
            else -> {
                result.complete(entry.content)
            }
        }

        return result.future()
    }

    override fun setBytes(key: String, value: ByteArray): Future<Unit> {
        val result = Promise.promise<Unit>()
        cacheImp.put(key, CacheEntry(value))
        result.complete()
        return result.future()
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long): Future<Unit> {
        val result = Promise.promise<Unit>()
        cacheImp.put(key, CacheEntry(value, expirationInMs))
        result.complete()
        return result.future()
    }
}