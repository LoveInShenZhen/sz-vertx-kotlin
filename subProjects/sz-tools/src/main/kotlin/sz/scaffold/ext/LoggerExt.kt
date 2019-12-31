package sz.scaffold.ext

//
// Created by kk on 2019/12/26.
//

import org.slf4j.Logger
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger as SzLogger


fun Logger.trace(item: Any) {
    this.trace(item.toShortJson())
}

fun Logger.debug(item: Any) {
    this.debug(item.toShortJson())
}

fun Logger.info(item: Any) {
    this.info(item.toShortJson())
}

fun Logger.warn(item: Any) {
    this.warn(item.toShortJson())
}

fun Logger.error(item: Any) {
    this.error(item.toShortJson())
}

fun SzLogger.trace(item: Any) {
    appLogger.trace(item)
}

fun SzLogger.debug(item: Any) {
    appLogger.debug(item)
}

fun SzLogger.info(item: Any) {
    appLogger.info(item)
}

fun SzLogger.warn(item: Any) {
    appLogger.warn(item)
}

fun SzLogger.error(item: Any) {
    appLogger.error(item)
}

