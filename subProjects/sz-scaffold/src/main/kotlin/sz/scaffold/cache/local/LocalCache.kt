package sz.scaffold.cache.local

import com.google.common.cache.Cache
import sz.scaffold.cache.CacheApi
import sz.scaffold.tools.SzException

//
// Created by kk on 2020/4/16.
//
@Suppress("DuplicatedCode")
class LocalCache(private val cacheImp: Cache<String, CacheEntry>) : CacheApi {
    override fun exists(key: String): Boolean {
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

    override fun del(key: String) {
        cacheImp.invalidate(key)
    }

    override fun getBytes(key: String): ByteArray {
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

    override fun getBytesOrElse(key: String, default: () -> ByteArray): ByteArray {
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

    override fun getBytesOrNull(key: String): ByteArray? {
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

    override fun setBytes(key: String, value: ByteArray) {
        cacheImp.put(key, CacheEntry(value))
    }

    override fun setBytes(key: String, value: ByteArray, expirationInMs: Long) {
        cacheImp.put(key, CacheEntry(value, expirationInMs))
    }
}