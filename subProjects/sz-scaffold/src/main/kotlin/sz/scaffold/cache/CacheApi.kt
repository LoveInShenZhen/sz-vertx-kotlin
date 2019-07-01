package sz.scaffold.cache

import jodd.datetime.JDateTime
import sz.scaffold.cache.redis.RedisCacheApi

//
// Created by kk on 17/9/4.
//

interface CacheApi {

    fun exists(key: String): Boolean

    fun get(key: String): String

    fun getOrElse(key: String, default: () -> String): String

    fun getOrNull(key: String): String?

    fun set(key: String, objJson: String, expirationInMs: Long)

    fun set(key: String, objJson: String)

    fun set(key: String, objJson: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            set(key, objJson)
        } else {
            set(key, objJson, diffTime)
        }
    }

    fun del(key: String)

    companion object {

        // 默认的 redis 缓存
        val redisCache = RedisCacheApi.default()

    }
}