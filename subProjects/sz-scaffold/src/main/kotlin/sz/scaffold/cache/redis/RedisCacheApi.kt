package sz.scaffold.cache.redis

import kotlinx.coroutines.runBlocking
import sz.scaffold.cache.AsyncCacheApi
import sz.scaffold.cache.CacheApi
import sz.scaffold.redis.kedis.pool.KedisPool
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger


@Suppress("OverridingDeprecatedMember", "DeprecatedCallableAddReplaceWith")
class RedisCacheApi(val name: String = "default") : CacheApi, AsyncCacheApi {

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: existsAwait")
    override fun exists(key: String): Boolean = runBlocking {
        existsAwait(key)
    }

    override suspend fun existsAwait(key: String): Boolean {
        return try {
            KedisPool.byName(name).borrow().use {
                it.existsAwait(listOf(key))!!.toInteger() == 1
            }
        } catch (ex: Exception) {
            false
        }
    }

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getAwait")
    override fun get(key: String): String = runBlocking {
        getAwait(key)
    }

    override suspend fun getAwait(key: String): String {
        return try {
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

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getOrElseAwait")
    override fun getOrElse(key: String, default: () -> String): String = runBlocking {
        getOrElseAwait(key, default)
    }

    override suspend fun getOrElseAwait(key: String, default: () -> String): String {
        return try {
            KedisPool.byName(name).borrow().use {
                it.getAwait(key)?.toString() ?: default()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            return default()
        }
    }

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getOrNullAwait")
    override fun getOrNull(key: String): String? = runBlocking {
        getOrNullAwait(key)
    }

    override suspend fun getOrNullAwait(key: String): String? {
        return try {
            KedisPool.byName(name).borrow().use {
                it.getAwait(key)?.toString()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            return null
        }
    }

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: setAwait")
    override fun set(key: String, objJson: String, expirationInMs: Long) = runBlocking {
        setAwait(key, objJson, expirationInMs)
    }

    override suspend fun setAwait(key: String, objJson: String, expirationInMs: Long) {
        try {
            KedisPool.byName(name).borrow().use {
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

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: setAwait")
    override fun set(key: String, objJson: String) = runBlocking {
        setAwait(key, objJson)
    }

    override suspend fun setAwait(key: String, objJson: String) {
        try {
            KedisPool.byName(name).borrow().use {
                it.setAwait(listOf(key, objJson))
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: delAwait")
    override fun del(key: String) = runBlocking {
        delAwait(key)
    }

    override suspend fun delAwait(key: String) {
        try {
            KedisPool.byName(name).borrow().use {
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

        fun default(): RedisCacheApi {
            return byName("default")
        }
    }
}