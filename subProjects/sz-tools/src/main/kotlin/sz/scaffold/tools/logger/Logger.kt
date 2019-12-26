package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory
import sz.scaffold.tools.logger.ext.debug
import sz.scaffold.tools.logger.ext.error
import sz.scaffold.tools.logger.ext.trace
import sz.scaffold.tools.logger.ext.warn

/**
 * Created by kk on 17/4/11.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Logger {

    val appLogger = LoggerFactory.getLogger("App")!!

    fun trace(msg: String) {
        appLogger.trace(msg)
    }

    fun trace(item: Any) {
        appLogger.trace(item)
    }

    fun debug(msg: String) {
        appLogger.debug(msg)
    }

    fun debug(item: Any) {
        appLogger.debug(item)
    }

    fun info(msg: String) {
        appLogger.info(msg)
    }

    fun info(item: Any) {
        appLogger.info(item.toString())
    }

    fun warn(msg: String) {
        appLogger.warn(msg)
    }

    fun warn(item: Any) {
        appLogger.warn(item)
    }

    fun error(msg: String) {
        appLogger.error(msg)
    }

    fun error(item: Any) {
        appLogger.error(item)
    }

    fun of(loggerName: String): org.slf4j.Logger {
        return LoggerFactory.getLogger(loggerName)
    }

    fun of(clazz: Class<*>): org.slf4j.Logger {
        return LoggerFactory.getLogger(clazz)
    }
}