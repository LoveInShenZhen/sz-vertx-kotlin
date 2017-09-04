package sz.scaffold.cache

import sz.scaffold.cache.redis.RedisCacheApi

//
// Created by kk on 17/9/4.
//
interface CacheApi {

    fun get(key: String): String

    fun getOrElse(key: String, default: () -> String): String

    fun getOrNull(key: String): String?

    fun set(key: String, objJson: String, expirationInMs: Long = 0)

    fun del(key: String)

    companion object {
        val redisCache = RedisCacheApi()

    }
}