package sz.scaffold.cache

import jodd.datetime.JDateTime

//
// Created by kk on 17/9/4.
//

interface CacheApi {

    fun exists(key: String): Boolean

    fun get(key: String): String

    fun getOrElse(key: String, default: () -> String): String

    fun getOrNull(key: String): String?

    fun set(key: String, valueTxt: String, expirationInMs: Long)

    fun set(key: String, valueTxt: String)

    fun set(key: String, valueTxt: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            set(key, valueTxt)
        } else {
            set(key, valueTxt, diffTime)
        }
    }

    fun del(key: String)
}