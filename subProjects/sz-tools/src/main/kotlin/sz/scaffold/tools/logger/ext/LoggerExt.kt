package sz.scaffold.tools.logger.ext

//
// Created by kk on 2019/12/26.
//

import org.slf4j.Logger
import sz.scaffold.tools.json.toShortJson


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

