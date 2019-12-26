package sz.scaffold.tools.logger.conversions

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import sz.scaffold.tools.logger.AnsiColor

//
// Created by kk on 17/8/20.
//
@Suppress("DuplicatedCode")
class ColoredLevel : ClassicConverter() {

    override fun convert(event: ILoggingEvent): String {
        return when (event.level) {
//            Level.TRACE -> "[${AnsiColor.cyan("trace")}"
            Level.DEBUG -> AnsiColor.blue("debug")
            Level.INFO -> AnsiColor.green("info")
            Level.WARN -> AnsiColor.yellow("warn")
            Level.ERROR -> AnsiColor.red("error")
            else -> ""
        }
    }
}