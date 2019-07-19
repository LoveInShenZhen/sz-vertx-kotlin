package sz.scaffold.cache.redis

import kotlinx.coroutines.runBlocking
import sz.scaffold.cache.AsyncCacheApi
import sz.scaffold.cache.CacheApi
import sz.scaffold.redis.kedis.pool.KedisPool
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger


class RedisCacheApi(val name: String = "default") : CacheApi, AsyncCacheApi {

    override fun exists(key: String): Boolean = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.existsAwait(listOf(key))!!.toInteger() == 1
            }
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun existsAwait(key: String): Boolean {
        return try {
            KedisPool.byName(name).borrowAwait().use {
                it.existsAwait(listOf(key))!!.toInteger() == 1
            }
        } catch (ex: Exception) {
            false
        }
    }

    override fun get(key: String): String = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.getAwait(key)?.toString() ?: throw SzException("$key 在缓存中不存在")
            }
        } catch (ex: SzException) {
            throw ex
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            throw ex
        }
    }

    override suspend fun getAwait(key: String): String {
        return try {
            KedisPool.byName(name).borrowAwait().use {
                it.getAwait(key)?.toString() ?: throw SzException("$key 在缓存中不存在")
            }
        } catch (ex: SzException) {
            throw ex
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            throw ex
        }
    }

    override fun getOrElse(key: String, default: () -> String): String = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.getAwait(key)?.toString() ?: default()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            default()
        }
    }

    override suspend fun getOrElseAwait(key: String, default: () -> String): String {
        return try {
            KedisPool.byName(name).borrowAwait().use {
                it.getAwait(key)?.toString() ?: default()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            default()
        }
    }

    override fun getOrNull(key: String): String? = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.getAwait(key)?.toString()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            null
        }
    }

    override suspend fun getOrNullAwait(key: String): String? {
        return try {
            KedisPool.byName(name).borrowAwait().use {
                it.getAwait(key)?.toString()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            null
        }
    }

    override fun set(key: String, objJson: String, expirationInMs: Long) = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                if (expirationInMs > 0) {
                    it.psetexAwait(key, expirationInMs.toString(), objJson)
                } else {
                    it.setAwait(listOf(key, objJson))
                }
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override suspend fun setAwait(key: String, objJson: String, expirationInMs: Long) {
        try {
            KedisPool.byName(name).borrowAwait().use {
                if (expirationInMs > 0) {
                    it.psetexAwait(key, expirationInMs.toString(), objJson)
                } else {
                    it.setAwait(listOf(key, objJson))
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun set(key: String, objJson: String) = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.setAwait(listOf(key, objJson))
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override suspend fun setAwait(key: String, objJson: String) {
        try {
            KedisPool.byName(name).borrowAwait().use {
                it.setAwait(listOf(key, objJson))
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun del(key: String) = runBlocking {
        try {
            KedisPool.byName(name).borrow().use {
                it.delAwait(listOf(key))
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override suspend fun delAwait(key: String) {
        try {
            KedisPool.byName(name).borrowAwait().use {
                it.delAwait(listOf(key))
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    companion object {

        private val instences = mutableMapOf<String, RedisCacheApi>()

        fun byName(name: String): RedisCacheApi {
            val cacheName = if (name.isBlank()) "default" else name
            return instences.getOrPut(cacheName) {
                if (KedisPool.exists(cacheName).not()) {
                    throw SzException("名称: $cacheName 对应的Redis配置不存在, 请检查配置文件.")
                }
                RedisCacheApi(cacheName)
            }
        }

        val default: RedisCacheApi by lazy {
            byName("default")
        }
    }
}