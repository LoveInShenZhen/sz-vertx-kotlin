package sz.scaffold.tools.logger.conversions

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import sz.scaffold.tools.logger.AnsiColor

//
// Created by kk on 2019/12/26.
//
@Suppress("DuplicatedCode")
class ColoredMessage : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        return when (event.level) {
            Level.TRACE -> AnsiColor.blue(event.message)
            Level.DEBUG -> AnsiColor.cyan(event.message)
            Level.INFO -> AnsiColor.white(event.message)
            Level.WARN -> AnsiColor.yellow(event.message)
            Level.ERROR -> AnsiColor.red(event.message)
            else -> event.message
        }
    }
}