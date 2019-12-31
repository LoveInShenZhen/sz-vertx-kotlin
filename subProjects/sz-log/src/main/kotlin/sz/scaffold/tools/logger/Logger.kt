package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Logger {

    val appLogger = LoggerFactory.getLogger("App")!!

    fun trace(msg: String) {
        appLogger.trace(msg)
    }

    fun debug(msg: String) {
        appLogger.debug(msg)
    }

    fun info(msg: String) {
        appLogger.info(msg)
    }

    fun warn(msg: String) {
        appLogger.warn(msg)
    }

    fun error(msg: String) {
        appLogger.error(msg)
    }

    fun of(loggerName: String): org.slf4j.Logger {
        return LoggerFactory.getLogger(loggerName)
    }

    fun of(clazz: Class<*>): org.slf4j.Logger {
        return LoggerFactory.getLogger(clazz)
    }
}