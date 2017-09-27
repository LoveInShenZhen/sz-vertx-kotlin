package sz.scaffold.tools

//
// Created by kk on 17/8/22.
//
class SzException(msg: String, cause: Throwable?) : RuntimeException(msg, cause) {

    constructor(ex: Exception) : this(ex.message ?: "", ex)

    constructor(msg: String) : this(msg, null)
}