package sz.scaffold.ext

import jodd.exception.ExceptionUtil
import jodd.io.FileNameUtil
import sz.scaffold.tools.logger.AnsiColor
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

//
// Created by kk on 17/8/24.
//

val zeroUUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

fun String?.escapeMarkdown(): String {
    if (this.isNullOrBlank()) return ""
    return this.replace("_", """\_""").replace("*", """\*""")
}

fun String.colorization(color: AnsiColor? = AnsiColor.BLUE, bgColog: AnsiColor? = null): String {
    if (color == null && bgColog == null) return this
    return "${color?.code ?: ""}${bgColog?.code ?: ""}${this}${AnsiColor.RESET.code}"
}

fun Exception.chainToString(): String {
    return ExceptionUtil.exceptionChainToString(this)
}

// ---------------------------------------------------------------- camel case

/**
 * Changes CamelCase string to lower case words separated by provided
 * separator character. The following translations are applied:
 *
 *  * Every upper case letter in the CamelCase name is translated into
 * two characters, a separator and the lower case equivalent of the target character,
 * with three exceptions.
 *  1. For contiguous sequences of upper case letters, characters after the first
 * character are replaced only by their lower case equivalent, and are not
 * preceded by a separator (`theFOO` to `the_foo`).
 *  1. An upper case character in the first position of the CamelCase name
 * is not preceded by a separator character, and is translated only to its
 * lower case equivalent. (`Foo` to `foo` and not `_foo`)
 *  1. An upper case character in the CamelCase name that is already preceded
 * by a separator character is translated only to its lower case equivalent,
 * and is not preceded by an additional separator. (`user_Name`
 * to `user_name` and not `user__name`.
 *
 *  * If the CamelCase name starts with a separator, then that
 * separator is not included in the translated name, unless the CamelCase
 * name is just one character in length, i.e., it is the separator character.
 * This applies only to the first character of the CamelCase name.
 *
 */
fun String.camelCaseToLowCaseSeprated(separator: Char): String {
    val length = this.length
    val result = StringBuilder(length * 2)
    var resultLength = 0
    var prevTranslated = false
    for (i in 0 until length) {
        var c = this[i]
        if (i > 0 || c != separator) {// skip first starting separator
            if (Character.isUpperCase(c)) {
                if (!prevTranslated && resultLength > 0 && result[resultLength - 1] != separator) {
                    result.append(separator)
                    resultLength++
                }
                c = Character.toLowerCase(c)
                prevTranslated = true
            } else {
                prevTranslated = false
            }
            result.append(c)
            resultLength++
        }
    }
    return if (resultLength > 0) result.toString() else this
}

/**
 * Converts separated string value to CamelCase.
 */
fun String.toCamelCase(firstCharUppercase: Boolean, separator: Char): String {
    val objStr = this.trimStart(separator)
    val length = objStr.length
    val sb = StringBuilder(length)
    var upperCase = firstCharUppercase

    for (i in 0 until length) {
        val ch = objStr[i]
        if (ch == separator) {
            upperCase = true
        } else if (upperCase) {
            sb.append(Character.toUpperCase(ch))
            upperCase = false
        } else {
            sb.append(ch)
        }
    }

    return sb.toString()
}

fun LocalDateTime.toEpochMs(zoneOffset: ZoneOffset = Zone.systemZoneOffset): Long {
    return this.toInstant(zoneOffset).toEpochMilli()
}

fun epochMsToLocalDateTime(epochMs: Long, zoneOffset: ZoneOffset = Zone.systemZoneOffset): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zoneOffset)
}

object Zone {
    val systemZoneOffset: ZoneOffset by lazy {
        OffsetDateTime.now().offset
    }
}

fun filePathJoin(vararg paths: String): String {
    if (paths.isEmpty()) {
        throw RuntimeException("no parameters")
    }

    if (paths.size == 1) {
        return paths[0]
    }

    var result = paths[0]
    for (index in 1 until paths.size) {
        result = FileNameUtil.concat(result, paths[index])
    }
    return result
}