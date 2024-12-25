package sz.scaffold.tools

import java.time.format.DateTimeFormatter

//
// Created by drago on 2024/12/19 周四.
//
object LocalDateTimeFormatter {
    val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val DATE_TIME_MS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
}