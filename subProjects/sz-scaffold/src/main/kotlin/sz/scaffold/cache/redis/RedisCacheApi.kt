package sz.scaffold.cache.redis

import sz.scaffold.cache.CacheApi
import sz.scaffold.ext.ChainToString
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/4.
//
class RedisCacheApi : CacheApi {

    override fun exists(key: String): Boolean {
        try {
            return JRedisPool.jedis().use {
                it.exists(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            return false
        }

    }

    override fun get(key: String): String {
        try {
            return JRedisPool.jedis().use {
                it.get(key) ?: throw SzException("$key 在缓存中不存在")
            }
        } catch (ex: SzException) {
            throw ex
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            throw ex
        }
    }

    override fun getOrElse(key: String, default: () -> String): String {
        try {
            return JRedisPool.jedis().use {
                if (!it.exists(key)) {
                    default()
                } else {
                    it.get(key)
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            return default()
        }
    }

    override fun getOrNull(key: String): String? {
        try {
            return JRedisPool.jedis().use {
                it.get(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            return null
        }
    }

    override fun set(key: String, objJson: String, expirationInMs: Long) {
        try {
            JRedisPool.jedis().use {
                it.psetex(key, expirationInMs, objJson)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
        }
    }

    override fun del(key: String) {
        try {
            JRedisPool.jedis().use {
                it.del(key)
            }
        } catch (ex:Exception) {
            Logger.error(ex.ChainToString())
        }
    }
}