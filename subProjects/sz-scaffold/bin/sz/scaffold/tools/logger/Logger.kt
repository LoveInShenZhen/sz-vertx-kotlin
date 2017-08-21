package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
object Logger {

    private val logger = LoggerFactory.getLogger("App")

    fun debug(msg:String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
        if (color != null || bgColog != null) {
            logger.debug("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
        } else {
            logger.debug(msg)
        }
    }

    fun info(msg:String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
        if (color != null || bgColog != null) {
            logger.info("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
        } else {
            logger.info(msg)
        }
    }

    fun warn(msg:String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
        if (color != null || bgColog != null) {
            logger.warn("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
        } else {
            logger.warn(msg)
        }
    }

    fun error(msg:String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
        if (color != null || bgColog != null) {
            logger.error("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
        } else {
            logger.error(msg)
        }
    }
}