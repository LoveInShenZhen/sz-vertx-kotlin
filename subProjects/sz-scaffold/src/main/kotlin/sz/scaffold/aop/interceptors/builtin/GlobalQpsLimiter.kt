package sz.scaffold.aop.interceptors.builtin

import jodd.util.Wildcard
import sz.scaffold.aop.interceptors.GlobalInterceptorBase
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.errors.builtin.SzErrors

// app.httpServer.interceptors 中的配置
// includes 和 excludes 采用通配符匹配, excludes 比 includes 的优先级高
// Possible patterns allow to match single characters ('?') or any count of characters ('*').
// Wildcard characters can be escaped (by an '\'). When matching path, deep tree wildcard also can be used ('**').
//{
//    className = "sz.scaffold.aop.interceptors.builtin.GlobalQpsLimiter"
//    config = {
//        name = "nameOfLimiter"
//        qps = 150
//        includes = ["/**"]
//        excludes = []
//    }
//}

@Suppress("UnstableApiUsage")
class GlobalQpsLimiter : GlobalInterceptorBase() {

    private val limiterName: String
        get() = config.getString("name")


    private fun match(path: String): Boolean {
        return config.getJsonArray("includes").any { Wildcard.match(path, it.toString()) } &&
            config.getJsonArray("excludes").any { Wildcard.match(path, it.toString()) }.not()
    }

    override suspend fun call(): Any? {
        val path = this.httpContext.request().path()

        return if (match(path)) {
            // this path need qps limiter checking
            val limiter = QpsLimiterMap.namedLimiterOf(limiterName)
            if (limiter.tryAcquire()) {
                delegate.call()
            } else {
                val reply = ReplyBase()
                reply.ret = SzErrors.ExceedQpsLimit.code
                reply.errmsg = "${SzErrors.ExceedQpsLimit.desc} [max ${limiter.rate} 次/秒]"
                reply
            }
        } else {
            delegate.call()
        }
    }
}