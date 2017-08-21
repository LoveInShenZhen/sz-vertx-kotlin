package sz.scaffold.tools.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

//
// Created by kk on 17/8/20.
//
class ColoredLevel : ClassicConverter() {

    override fun convert(event: ILoggingEvent?): String {
        return when (event!!.level) {
            Level.TRACE -> "[${AnsiColor.blue("trace")}"
            Level.DEBUG -> "[${AnsiColor.cyan("debug")}]"
            Level.INFO -> "[${AnsiColor.white("info")}]"
            Level.WARN -> "[${AnsiColor.yellow("warn")}]"
            Level.ERROR -> "[${AnsiColor.red("error")}]"
            else -> ""
        }
    }
}