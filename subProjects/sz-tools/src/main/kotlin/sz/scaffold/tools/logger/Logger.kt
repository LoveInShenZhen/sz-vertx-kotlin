package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Logger {

    var enableColor = true

    private val logger = LoggerFactory.getLogger("App")

    private val defaultDebugColor: AnsiColor? by lazy {
        if (enableColor) AnsiColor.BLUE else null
    }

    private val defaultInfoColor: AnsiColor? by lazy {
        if (enableColor) AnsiColor.GREEN else null
    }

    private val defaultWarnColor: AnsiColor? by lazy {
        if (enableColor) AnsiColor.YELLOW else null
    }

    private val defaultErrColor: AnsiColor? by lazy {
        if (enableColor) AnsiColor.RED else null
    }

    fun debug(msg: String, color: AnsiColor? = defaultDebugColor, bgColog: AnsiColor? = null) {
        logger.colorDebug(msg, color, bgColog)
    }

    fun info(msg: String, color: AnsiColor? = defaultInfoColor, bgColog: AnsiColor? = null) {
        logger.colorInfo(msg, color, bgColog)
    }

    fun warn(msg: String, color: AnsiColor? = defaultWarnColor, bgColog: AnsiColor? = null) {
        logger.colorWarn(msg, color, bgColog)
    }

    fun error(msg: String, color: AnsiColor? = defaultErrColor, bgColog: AnsiColor? = null) {
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

fun org.slf4j.Logger.colorInfo(msg: String, color: AnsiColor? = AnsiColor.GREEN, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.info("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.info(msg)
    }
}

fun org.slf4j.Logger.colorWarn(msg: String, color: AnsiColor? = AnsiColor.YELLOW, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.warn("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.warn(msg)
    }
}

fun org.slf4j.Logger.colorError(msg: String, color: AnsiColor? = AnsiColor.RED, bgColog: AnsiColor? = null) {
    if (color != null || bgColog != null) {
        this.error("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
    } else {
        this.error(msg)
    }
}