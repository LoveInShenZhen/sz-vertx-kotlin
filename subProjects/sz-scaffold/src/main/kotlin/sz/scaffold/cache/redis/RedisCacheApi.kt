package sz.scaffold.cache.redis

import sz.scaffold.cache.CacheApi
import sz.scaffold.tools.SzException

//
// Created by kk on 17/9/4.
//
class RedisCacheApi : CacheApi {

    override fun get(key: String): String {
        return JRedisPool.jedis().use {
            it.get(key) ?: throw SzException("$key 在缓存中不存在")
        }
    }

    override fun getOrElse(key: String, default: () -> String): String {
        return JRedisPool.jedis().use {
            if (!it.exists(key)) {
                default()
            } else {
                it.get(key)
            }
        }
    }

    override fun getOrNull(key: String): String? {
        return JRedisPool.jedis().use {
            it.get(key)
        }
    }

    override fun set(key: String, objJson: String, expirationInMs: Long) {
        JRedisPool.jedis().use {
            it.psetex(key, expirationInMs, objJson)
        }
    }

    override fun del(key: String) {
        JRedisPool.jedis().use {
            it.del(key)
        }
    }
}