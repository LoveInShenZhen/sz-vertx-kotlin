package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
object Logger {

    var enableColor = true

    private val logger = LoggerFactory.getLogger("App")

    private fun defaultColor(): AnsiColor? {
        return if (enableColor) AnsiColor.BLUE else null
    }

    fun debug(msg: String, color: AnsiColor? = defaultColor(), bgColog: AnsiColor? = null) {
        logger.colorDebug(msg, color, bgColog)
    }

    fun info(msg: String, color: AnsiColor? = defaultColor(), bgColog: AnsiColor? = null) {
        logger.colorInfo(msg, color, bgColog)
    }

    fun warn(msg: String, color: AnsiColor? = defaultColor(), bgColog: AnsiColor? = null) {
        logger.colorWarn(msg, color, bgColog)
    }

    fun error(msg: String, color: AnsiColor? = defaultColor(), bgColog: AnsiColor? = null) {
        logger.colorError(msg, color, bgColog)
    }

    fun of(loggerName: String): org.slf4j.Logger {
        return LoggerFactory.getLogger(loggerName)
    }

    fun of(clazz: Class<*>): org.slf4j.Logger {
        return LoggerFactory.getLogger(clazz)
    }
}

fun org.slf4j.Logger.colorDebug(msg: String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.debug("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.debug(msg)
    }
}

fun org.slf4j.Logger.colorInfo(msg: String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.info("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.info(msg)
    }
}

fun org.slf4j.Logger.colorWarn(msg: String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.warn("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.warn(msg)
    }
}

fun org.slf4j.Logger.colorError(msg: String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.error("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.error(msg)
    }
}