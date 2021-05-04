package sz.cache

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.await
import jodd.datetime.JDateTime
import sz.scaffold.tools.logger.Logger
import kotlin.math.exp

//
// Created by kk on 2019-06-14.
//
interface AsyncCacheApi {

    fun exists(key: String): Future<Boolean>

    fun del(key: String): Future<Unit>

    //<editor-fold desc="For String Value">
    fun get(key: String): Future<String> {
        val result = Promise.promise<String>()
        getBytes(key).onSuccess { bytes ->
            result.complete(bytes.toString(Charsets.UTF_8))
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun getOrElse(key: String, default: () -> String): Future<String> {
        val result = Promise.promise<String>()
        getBytesOrNull(key).onSuccess { bytes ->
            if (bytes == null) {
                result.complete(default())
            } else {
                result.complete(bytes.toString(Charsets.UTF_8))
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun getOrNull(key: String): Future<String?> {
        val result = Promise.promise<String?>()
        getBytesOrNull(key).onSuccess { bytes ->
            if (bytes == null) {
                result.complete(null)
            } else {
                result.complete(bytes.toString(Charsets.UTF_8))
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun getOrPut(key: String, expirationInMs: Long = 0, supplier: () -> String): Future<String> {
        val result = Promise.promise<String>()
        getOrNull(key).onSuccess { value ->
            if (value != null) {
                result.complete(value)
            } else {
                val newValue = supplier()
                set(key, newValue, expirationInMs)
                result.complete(newValue)
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun set(key: String, value: String): Future<Unit> {
        val result = Promise.promise<Unit>()

        setBytes(key, value.toByteArray(Charsets.UTF_8)).onSuccess {
            result.complete()
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun set(key: String, value: String, expirationInMs: Long): Future<Unit> {
        val result = Promise.promise<Unit>()

        setBytes(key, value.toByteArray(Charsets.UTF_8), expirationInMs).onSuccess {
            result.complete()
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun set(key: String, value: String, cleaningTime: JDateTime): Future<Unit> {
        val result = Promise.promise<Unit>()

        setBytes(key, value.toByteArray(Charsets.UTF_8), cleaningTime).onSuccess {
            result.complete()
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }
    //</editor-fold>

    //<editor-fold desc="For Bytes Value">
    fun getBytes(key: String): Future<ByteArray>

    fun getBytesOrElse(key: String, default: () -> ByteArray): Future<ByteArray>

    fun getBytesOrNull(key: String): Future<ByteArray?>

    fun getBytesOrPut(key: String, expirationInMs: Long = 0, supplier: () -> ByteArray): Future<ByteArray> {
        val result = Promise.promise<ByteArray>()

        getBytesOrNull(key).onSuccess { bytes ->
            if (bytes == null) {
                val newValue = supplier()
                setBytes(key, newValue, expirationInMs).onFailure { ex ->
                    Logger.warn(ex.localizedMessage)
                }
                result.complete(newValue)
            } else {
                result.complete(bytes)
            }
        }.onFailure { ex ->
            result.fail(ex)
        }

        return result.future()
    }

    fun setBytes(key: String, value: ByteArray): Future<Unit>

    fun setBytes(key: String, value: ByteArray, expirationInMs: Long): Future<Unit>

    fun setBytes(key: String, value: ByteArray, cleaningTime: JDateTime): Future<Unit> {
        val result = Promise.promise<Unit>()
        val now = JDateTime().timeInMillis
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime > 0) {
            setBytes(key, value, diffTime).onSuccess {
                result.complete()
            }.onFailure { ex ->
                result.fail(ex)
            }
        }
        return result.future()
    }
    //</editor-fold>

}