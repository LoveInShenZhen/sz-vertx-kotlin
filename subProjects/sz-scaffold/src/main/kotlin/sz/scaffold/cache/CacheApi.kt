package sz.scaffold.cache

import jodd.datetime.JDateTime
import sz.scaffold.cache.redis.RedisCacheApi

//
// Created by kk on 17/9/4.
//

interface CacheApi {

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: existsAwait")
    fun exists(key: String): Boolean

    suspend fun existsAwait(key: String):Boolean

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getAwait")
    fun get(key: String): String

    suspend fun getAwait(key: String): String

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getOrElseAwait")
    fun getOrElse(key: String, default: () -> String): String

    suspend fun getOrElseAwait(key: String, default: () -> String): String

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: getOrNullAwait")
    fun getOrNull(key: String): String?

    suspend fun getOrNullAwait(key: String): String?

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: setAwait")
    fun set(key: String, objJson: String, expirationInMs: Long)

    suspend fun setAwait(key: String, objJson: String, expirationInMs: Long)

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: setAwait")
    fun set(key: String, objJson: String)

    suspend fun setAwait(key: String, objJson: String)

    @Suppress("DEPRECATION")
    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: setAwait")
    fun set(key: String, objJson: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            set(key, objJson)
        } else {
            set(key, objJson, diffTime)
        }
    }

    suspend fun setAwait(key: String, objJson: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            setAwait(key, objJson)
        } else {
            setAwait(key, objJson, diffTime)
        }
    }

    @Deprecated("同步方式的接口, 为了兼容老版本才保留,请改用异步接口: delAwait")
    fun del(key: String)

    suspend fun delAwait(key: String)

    companion object {
        val redisCache = RedisCacheApi()
//        val localCache = TODO()

    }
}