package sz.scaffold.aop.interceptors


import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.tools.logger.Logger

@WithAction(SampleCheckTokenAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SampleCheckToken(
        val token: String = "token",
        val roles: IntArray = intArrayOf()
)

class SampleCheckTokenAction : Action<SampleCheckToken>() {

    override suspend fun call(): Any? {
        Logger.debug(" CheckTokenAction.call() ... token: ${this.config.token}, require roles: [${this.config.roles.joinToString(",")}]")
        // 以下2行是模拟检查token不通过, 直接结束掉当前的request处理.
//        this.httpContext.response().end("token timeout ...")
//        return Unit

        // 以下4行是模拟检查token不通过, 返回一个对应错误的 reply
//        val reply = ReplyBase()
//        reply.ret = -1
//        reply.errmsg = "token timeout"
//        return reply

        // 当前拦截器要做的事情已经做完, 则把请求继续交给下一个 delegate 来处理
        return delegate.call()
    }
}