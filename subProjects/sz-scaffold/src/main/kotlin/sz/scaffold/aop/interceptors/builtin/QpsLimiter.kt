package sz.scaffold.aop.interceptors.builtin

//
// Created by kk on 2019-06-27.
//

import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.ApiRoute.Companion.queryParams
import sz.scaffold.controller.ContentTypes
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.errors.builtin.SzErrors
import sz.scaffold.tools.json.Json

@WithAction(QpsLimiterAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QpsLimiter(
    val qps: Double
)

class QpsLimiterAction : Action<QpsLimiter>() {

    override suspend fun call(): Any? {
        val limiter = QpsLimiterMap.apiLimiterOf(this.httpContext.request().path())
        if (limiter.tryAcquire()) {
            return delegate.call()
        } else {
            val reply = ReplyBase()
            reply.ret = SzErrors.ExceedQpsLimit.code
            reply.errmsg = "${SzErrors.ExceedQpsLimit.desc} [max ${limiter.rate} 次/秒]"
            if (isJsonpRequest()) {
                onJsonp(reply)
                return null
            } else {
                return reply
            }
        }
    }

    private fun onJsonp(result: Any) {
        val response = httpContext.response()
        response.putHeader("Content-Type", ContentTypes.JavaScript)
        val callback = httpContext.queryParams(mapOf()).getValue("callback")
        val body = "$callback(${Json.toJsonStrPretty(result)});"
        response.write(body)
    }

    private fun isJsonpRequest(): Boolean {
        return httpContext.queryParams(mapOf()).containsKey("callback")
    }
}