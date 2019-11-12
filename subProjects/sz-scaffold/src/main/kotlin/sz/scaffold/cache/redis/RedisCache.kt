package sz.scaffold.cache.redis

import kotlinx.coroutines.runBlocking
import sz.scaffold.cache.CacheApi
import sz.scaffold.redis.kedis.KedisPool
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger


@Suppress("MemberVisibilityCanBePrivate")
class RedisCache(val name: String = "default") : CacheApi {

    override fun exists(key: String): Boolean = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.existsAwait(listOf(key))!!.toInteger() == 1
            }
        } catch (ex: Exception) {
            false
        }
    }

    override fun get(key: String): String = runBlocking {
        try {
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

    override fun getOrElse(key: String, default: () -> String): String = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.getAwait(key)?.toString() ?: default()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            default()
        }
    }

    override fun getOrNull(key: String): String? = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.getAwait(key)?.toString()
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            null
        }
    }

    override fun set(key: String, valueTxt: String, expirationInMs: Long) = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                if (expirationInMs > 0) {
                    it.psetexAwait(key, expirationInMs.toString(), valueTxt)
                } else {
                    it.setAwait(listOf(key, valueTxt))
                }
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun set(key: String, valueTxt: String) = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.setAwait(listOf(key, valueTxt))
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun del(key: String) = runBlocking {
        try {
            KedisPool.byName(name).borrowAwait().target.use {
                it.delAwait(listOf(key))
            }
            Unit
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    companion object {

        private val instences = mutableMapOf<String, RedisCache>()

        fun byName(name: String): RedisCache {
            val cacheName = if (name.isBlank()) "default" else name
            return instences.getOrPut(cacheName) {
                if (KedisPool.exists(cacheName).not()) {
                    throw SzException("名称: $cacheName 对应的Redis配置不存在, 请检查配置文件.")
                }
                RedisCache(cacheName)
            }
        }

        val default: RedisCache by lazy {
            byName("default")
        }
    }
}