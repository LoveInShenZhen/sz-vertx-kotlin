package sz.scaffold.controller.reply

import jodd.exception.ExceptionUtil
import sz.scaffold.annotations.Comment
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.json.Json

//
// Created by kk on 17/8/16.
//

open class ReplyBase {

    @Comment("返回的错误码, 0: 成功, 非0: 错误")
    var ret: Int = 0

    @Comment("ret=0时, 返回OK, 非0时, 返回错误描述信息")
    var errmsg: String = "OK"

    fun SampleData() {
        ret = 0
        errmsg = "OK"
    }

    fun onError(ex: Throwable) {
        if (ex is BizLogicException) {
            this.ret = ex.ErrCode
            this.errmsg = ex.message!!
        } else {
            this.ret = -1
            this.errmsg = ExceptionUtil.exceptionStackTraceToString(ex)
        }
    }

    fun successed(): Boolean {
        return ret == 0
    }

    fun failed(): Boolean {
        return successed().not()
    }

    override fun toString(): String {
        return Json.toJsonStrPretty(this)
    }
}