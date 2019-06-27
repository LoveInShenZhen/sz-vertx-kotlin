package sz.scaffold.aop.interceptors.builtin

//
// Created by kk on 2019-06-27.
//

import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.errors.builtin.SzErrors

@WithAction(QpsLimiterAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class QpsLimiter(
    val qps: Double
)

class QpsLimiterAction : Action<QpsLimiter>() {

    override suspend fun call(): Any? {
        val limiter = QpsLimiterMap.limiterOf(this.httpContext.request().path())
        if (limiter.tryAcquire()) {
            return delegate.call()
        } else {
            val reply = ReplyBase()
            reply.ret = SzErrors.ExceedQpsLimit.code
            reply.errmsg = "${SzErrors.ExceedQpsLimit.desc } [max ${limiter.rate} 次/秒]"
            return reply
        }
        // 当前拦截器要做的事情已经做完, 则把请求继续交给下一个 delegate 来处理

    }
}