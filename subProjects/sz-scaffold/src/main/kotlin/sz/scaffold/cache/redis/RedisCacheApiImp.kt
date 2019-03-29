package sz.scaffold.cache.redis

import sz.scaffold.cache.CacheApi
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2018/10/21.
//
class RedisCacheApiImp(val name:String) : CacheApi {

    override fun exists(key: String): Boolean {
        try {
            return JRedisPool.byName(name).jedis().use {
                it.exists(key)
            }
        } catch (ex: Exception) {
            return false
        }

    }

    override fun get(key: String): String {
        try {
            return JRedisPool.byName(name).jedis().use {
                it.get(key) ?: throw SzException("$key 在缓存中不存在")
            }
        } catch (ex: SzException) {
            throw ex
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            throw ex
        }
    }

    override fun getOrElse(key: String, default: () -> String): String {
        try {
            return JRedisPool.byName(name).jedis().use {
                if (!it.exists(key)) {
                    default()
                } else {
                    it.get(key)
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            return default()
        }
    }

    override fun getOrNull(key: String): String? {
        try {
            return JRedisPool.byName(name).jedis().use {
                it.get(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
            return null
        }
    }

    override fun set(key: String, objJson: String, expirationInMs: Long) {
        try {
            JRedisPool.byName(name).jedis().use {
                if (expirationInMs > 0) {
                    it.psetex(key, expirationInMs, objJson)
                } else {
                    it.set(key, objJson)
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun set(key: String, objJson: String) {
        try {
            JRedisPool.byName(name).jedis().use {
                it.set(key, objJson)
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }

    override fun del(key: String) {
        try {
            JRedisPool.byName(name).jedis().use {
                it.del(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.localizedMessage)
        }
    }
}