package jodd.datetime.ext

import jodd.datetime.JDateTime
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

fun Date.toJDateTime(): JDateTime {
    return JDateTime(this)
}