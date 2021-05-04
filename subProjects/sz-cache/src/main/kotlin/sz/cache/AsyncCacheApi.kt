package sz.cache

import jodd.datetime.JDateTime

//
// Created by kk on 2019-06-14.
//
interface AsyncCacheApi {

    suspend fun existsAwait(key: String): Boolean

    suspend fun delAwait(key: String)

    //<editor-fold desc="For String Value">
    suspend fun getAwait(key: String): String {
        return getBytesAwait(key).toString(Charsets.UTF_8)
    }

    suspend fun getOrElseAwait(key: String, default: () -> String): String {
        val value = getBytesOrNullAwait(key)
        return value?.toString(Charsets.UTF_8) ?: default()
    }

    suspend fun getOrNullAwait(key: String): String? {
        val value = getBytesOrNullAwait(key)
        return value?.toString(Charsets.UTF_8)
    }

    suspend fun getOrPutAwait(key: String, expirationInMs: Long = 0, supplier: () -> String): String {
        val value = getOrNullAwait(key)
        return if (value == null) {
            val newValue = supplier()
            setAwait(key, newValue, expirationInMs)
            newValue
        } else {
            value
        }
    }

    suspend fun setAwait(key: String, value: String) {
        setBytesAwait(key, value.toByteArray(Charsets.UTF_8))
    }

    suspend fun setAwait(key: String, value: String, expirationInMs: Long) {
        setBytesAwait(key, value.toByteArray(Charsets.UTF_8), expirationInMs)
    }

    suspend fun setAwait(key: String, value: String, cleaningTime: JDateTime) {
        setBytesAwait(key, value.toByteArray(Charsets.UTF_8), cleaningTime)
    }
    //</editor-fold>

    //<editor-fold desc="For Bytes Value">
    suspend fun getBytesAwait(key: String): ByteArray

    suspend fun getBytesOrElseAwait(key: String, default: () -> ByteArray): ByteArray

    suspend fun getBytesOrNullAwait(key: String): ByteArray?

    suspend fun getBytesOrPutAwait(key: String, expirationInMs: Long = 0, supplier: () -> ByteArray): ByteArray {
        val value = getBytesOrNullAwait(key)
        return if (value == null) {
            val newValue = supplier()
            setBytesAwait(key, newValue, expirationInMs)
            newValue
        } else {
            value
        }
    }

    suspend fun setBytesAwait(key: String, value: ByteArray)

    suspend fun setBytesAwait(key: String, value: ByteArray, expirationInMs: Long)

    suspend fun setBytesAwait(key: String, value: ByteArray, cleaningTime: JDateTime) {
        val now = JDateTime().timeInMillis
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            setBytesAwait(key, value)
        } else {
            setBytesAwait(key, value, diffTime)
        }
    }
    //</editor-fold>

}