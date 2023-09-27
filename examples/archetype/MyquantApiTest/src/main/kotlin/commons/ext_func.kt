package commons

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.TimeZone

//
// Created by drago on 2023/9/11 011.
//


fun com.google.protobuf.Timestamp.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(this.seconds, this.nanos, ZoneOffset.ofHours(8))
}

fun com.google.protobuf.Timestamp.toLocalDate(): LocalDate {
    return this.toLocalDateTime().toLocalDate()
}