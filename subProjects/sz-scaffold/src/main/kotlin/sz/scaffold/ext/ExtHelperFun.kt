package sz.scaffold.ext

import jodd.exception.ExceptionUtil
import sz.scaffold.tools.logger.AnsiColor

//
// Created by kk on 17/8/24.
//

fun String?.escapeMarkdown(): String {
    if (this.isNullOrBlank()) return ""
    return this!!.replace("_", """\_""").replace("*", """\*""")
}

fun String.Colorization(color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null): String {
    if (color == null && bgColog == null) return this
    return "${color?.code ?: ""}${bgColog?.code ?: ""}${this}${AnsiColor.RESET.code}"
}

fun Exception.ChainToString(): String {
    return ExceptionUtil.exceptionChainToString(this)
}