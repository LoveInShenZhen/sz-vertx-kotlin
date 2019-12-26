package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Logger {

    private val logger = LoggerFactory.getLogger("App")

    fun debug(msg: String) {
        logger.debug(msg)
    }

    fun info(msg: String) {
        logger.info(msg)
    }

    fun warn(msg: String) {
        logger.warn(msg)
    }

    fun error(msg: String) {
        logger.error(msg)
    }

    fun of(loggerName: String): org.slf4j.Logger {
        return LoggerFactory.getLogger(loggerName)
    }

    fun of(clazz: Class<*>): org.slf4j.Logger {
        return LoggerFactory.getLogger(clazz)
    }
}