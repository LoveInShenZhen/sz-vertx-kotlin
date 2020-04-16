package sz.scaffold.cache.local

import com.google.common.cache.Cache
import sz.scaffold.cache.AsyncCacheApi
import sz.scaffold.tools.SzException

//
// Created by kk on 2020/4/16.
//
@Suppress("DuplicatedCode")
class LocalAsyncCache(private val cacheImp: Cache<String, CacheEntry>) : AsyncCacheApi {
    override suspend fun existsAwait(key: String): Boolean {
        val entry = cacheImp.getIfPresent(key)
        return when {
            entry == null -> {
                false
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                false
            }
            else -> {
                true
            }
        }
    }

    override suspend fun delAwait(key: String) {
        cacheImp.invalidate(key)
    }

    override suspend fun getBytesAwait(key: String): ByteArray {
        val entry = cacheImp.getIfPresent(key)
        when {
            entry == null -> {
                throw SzException("'$key' does not exist in the cache.")
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                throw SzException("'$key' does not exist in the cache.")
            }
            else -> {
                return entry.content
            }
        }
    }

    override suspend fun getBytesOrElseAwait(key: String, default: () -> ByteArray): ByteArray {
        val entry = cacheImp.getIfPresent(key)
        return when {
            entry == null -> {
                default()
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                default()
            }
            else -> {
                entry.content
            }
        }
    }

    override suspend fun getBytesOrNullAwait(key: String): ByteArray? {
        val entry = cacheImp.getIfPresent(key)
        return when {
            entry == null -> {
                null
            }
            entry.isExpired() -> {
                cacheImp.invalidate(key)
                null
            }
            else -> {
                entry.content
            }
        }
    }

    override suspend fun setBytesAwait(key: String, value: ByteArray) {
        cacheImp.put(key, CacheEntry(value))
    }

    override suspend fun setBytesAwait(key: String, value: ByteArray, expirationInMs: Long) {
        cacheImp.put(key, CacheEntry(value, expirationInMs))
    }
}