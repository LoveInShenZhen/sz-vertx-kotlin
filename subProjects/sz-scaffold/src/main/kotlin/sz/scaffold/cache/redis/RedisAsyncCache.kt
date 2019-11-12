package sz.scaffold.cache.redis

import sz.scaffold.cache.AsyncCacheApi
import sz.scaffold.redis.kedis.KedisPool
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019/11/12.
//
@Suppress("MemberVisibilityCanBePrivate")
class RedisAsyncCache(val name: String = "default") : AsyncCacheApi {

    override suspend fun existsAwait(key: String): Boolean {
        return try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.existsAwait(listOf(key))!!.toInteger() == 1
            }
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun getAwait(key: String): String {
        return try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.getAwait(key)?.toString() ?: throw SzException("$key 在缓存中不存在")
            }
        } catch (ex: SzException) {
            throw ex
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            throw ex
        }
    }

    override suspend fun getOrElseAwait(key: String, default: () -> String): String {
        return try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.getAwait(key)?.toString() ?: default()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            default()
        }
    }

    override suspend fun getOrNullAwait(key: String): String? {
        return try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.getAwait(key)?.toString()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            null
        }
    }

    override suspend fun setAwait(key: String, valueTxt: String, expirationInMs: Long) {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                if (expirationInMs > 0) {
                    it.psetexAwait(key, expirationInMs.toString(), valueTxt)
                } else {
                    it.setAwait(listOf(key, valueTxt))
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override suspend fun setAwait(key: String, valueTxt: String) {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.setAwait(listOf(key, valueTxt))
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override suspend fun delAwait(key: String) {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.delAwait(listOf(key))
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    companion object {

        private val instences = mutableMapOf<String, RedisAsyncCache>()

        fun byName(name: String): RedisAsyncCache {
            val cacheName = if (name.isBlank()) "default" else name
            return instences.getOrPut(cacheName) {
                if (KedisPool.exists(cacheName).not()) {
                    throw SzException("名称: $cacheName 对应的Redis配置不存在, 请检查配置文件.")
                }
                RedisAsyncCache(cacheName)
            }
        }

        val default: RedisAsyncCache by lazy {
            byName("default")
        }
    }
}