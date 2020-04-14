package jodd.datetime.ext

import jodd.datetime.JDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

//
// Created by kk on 2018/10/14.
//

fun JDateTime.firstDayOfMonth(): JDateTime {
    val result = this.clone()
    result.day = 1
    return result
}

fun JDateTime.lastDayOfMonth(): JDateTime {
    val result = this.clone()
    result.day = result.monthLength
    return result
}

fun JDateTime.firstDayOfYear(): JDateTime {
    val result = this.clone()
    result.month = 1
    result.day = 1
    return result
}

fun JDateTime.lastDayOfYear(): JDateTime {
    val result = this.clone()
    result.month = 12
    result.day = result.monthLength

    return result
}

fun JDateTime.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(this.year, this.month, this.dayOfMonth, this.hour, this.minute, this.second, this.millisecond * 1_000_000)
}

fun Date.toJDateTime(): JDateTime {
    return JDateTime(this)
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

fun LocalDateTime.toJDateTime(): JDateTime {
    return JDateTime(this.toEpochMs())
}

