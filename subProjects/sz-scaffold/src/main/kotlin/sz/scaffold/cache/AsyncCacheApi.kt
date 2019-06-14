package sz.scaffold.cache

import jodd.datetime.JDateTime

//
// Created by kk on 2019-06-14.
//
interface AsyncCacheApi {

    suspend fun existsAwait(key: String): Boolean

    suspend fun getAwait(key: String): String

    suspend fun getOrElseAwait(key: String, default: () -> String): String

    suspend fun getOrNullAwait(key: String): String?

    suspend fun setAwait(key: String, objJson: String, expirationInMs: Long)

    suspend fun setAwait(key: String, objJson: String)

    suspend fun setAwait(key: String, objJson: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            setAwait(key, objJson)
        } else {
            setAwait(key, objJson, diffTime)
        }

    }

    suspend fun delAwait(key: String)
}