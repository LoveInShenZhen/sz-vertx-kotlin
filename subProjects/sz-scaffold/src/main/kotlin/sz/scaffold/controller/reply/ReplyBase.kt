package sz.scaffold.controller.reply

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.ApiModel
import jodd.exception.ExceptionUtil
import sz.scaffold.annotations.Comment
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.json.Json

//
// Created by kk on 17/8/16.
//

@ApiModel
open class ReplyBase {

    @Comment("返回的错误码, 0: 成功, 非0: 错误")
    var ret: Int = 0

    @Comment("ret=0时, 返回OK, 非0时, 返回错误描述信息")
    var errmsg: String = "OK"

    @Comment("ret 非0时, 附加的错误信息, Json 格式")
    var errors: JsonNode? = null

    open fun SampleData() {
        ret = 0
        errmsg = "OK"
        errors = null
    }

    fun OnError(ex: Throwable) {
        if (ex is BizLogicException) {
            this.ret = ex.ErrCode
            this.errmsg = ex.message!!
        } else {
            this.ret = -1
            this.errmsg = ExceptionUtil.exceptionStackTraceToString(ex)
        }
    }

    override fun toString(): String {
        return Json.toJsonStrPretty(this)
    }
}