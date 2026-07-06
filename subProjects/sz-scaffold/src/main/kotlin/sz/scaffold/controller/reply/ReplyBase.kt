package sz.scaffold.controller.reply

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import jodd.exception.ExceptionUtil
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.ext.safeString
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

    open fun SampleData() {
        if (this.javaClass.name == ReplyBase::class.java.name) {
            ret = 0
            errmsg = "OK"
        } else {
            // 是子类, 提示研发工程师override 实现此方法, 配合自动生成文档接口中的sample数据
            ret = -1
            errmsg =
                "请在 ${this.javaClass.name} 实现 override fun SampleData() { TODO() } 方法, 配合自动生成文档接口中的sample数据"
        }
    }

    fun onError(ex: Throwable) {
        if (ex is BizLogicException) {
            this.ret = ex.ErrCode
            this.errmsg = ex.message!!
            return
        } else {
            val causeByBizLogicException = ExceptionUtil.findCause(ex, BizLogicException::class.java)
            if (causeByBizLogicException != null) {
                this.ret = causeByBizLogicException.ErrCode
                this.errmsg = causeByBizLogicException.message!!
                return
            }

            val causeByUnrecognizedPropertyException =
                ExceptionUtil.findCause(ex, UnrecognizedPropertyException::class.java)
            if (causeByUnrecognizedPropertyException != null) {
                this.ret = -1
                this.errmsg = causeByUnrecognizedPropertyException.message!!
                return
            }

            val illegalArgumentException = ExceptionUtil.findCause(ex, IllegalArgumentException::class.java)
            if (illegalArgumentException != null) {
                this.ret = -1
                this.errmsg = illegalArgumentException.message!!
                return
            }

            // 其他异常
            this.ret = -1
            this.errmsg =
                if (Application.inProductionMode) ex.message.safeString() else ExceptionUtil.exceptionChainToString(ex)

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