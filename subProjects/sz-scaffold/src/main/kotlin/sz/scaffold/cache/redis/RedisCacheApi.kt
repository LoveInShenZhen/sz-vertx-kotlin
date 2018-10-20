package sz.scaffold.cache.redis

import sz.scaffold.cache.CacheApi
import sz.scaffold.ext.ChainToString
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/4.
//
class RedisCacheApi(val name:String = "default") : CacheApi {

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
            Logger.error(ex.ChainToString())
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
            Logger.error(ex.ChainToString())
            return default()
        }
    }

    override fun getOrNull(key: String): String? {
        try {
            return JRedisPool.byName(name).jedis().use {
                it.get(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
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
            Logger.error(ex.ChainToString())
        }
    }

    override fun set(key: String, objJson: String) {
        try {
            JRedisPool.byName(name).jedis().use {
                it.set(key, objJson)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
        }
    }

    override fun del(key: String) {
        try {
            JRedisPool.byName(name).jedis().use {
                it.del(key)
            }
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
        }
    }

    // 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live), 单位: 秒
    fun ttl(key: String): Long {
        try {
            return JRedisPool.byName(name).jedis().ttl(key)
        } catch (ex: Exception) {
            Logger.error(ex.ChainToString())
            return -1
        }
    }

    fun update(key: String, value: String) {
        val ttl = this.ttl(key)
        if (ttl == (-1).toLong()) return        // 获取ttl失败
        this.set(key = key, objJson = value, expirationInMs = ttl * 1000)
    }
}