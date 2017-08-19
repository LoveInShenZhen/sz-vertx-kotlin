package sz.scaffold.tools.logger

import org.slf4j.LoggerFactory

/**
 * Created by kk on 17/4/11.
 */
object Logger {

    private val logger = LoggerFactory.getLogger("debug")

    fun debug(msg:String, color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null) {
        if (color != null || bgColog != null) {
            logger.debug("${color?.code ?: ""}${bgColog?.code ?: ""}${msg}${AnsiColor.RESET.code}")
        } else {
            logger.debug(msg)
        }
    }
}