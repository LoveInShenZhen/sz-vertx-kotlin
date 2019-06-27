package sz.scaffold.aop.interceptors.builtin

import io.vertx.core.json.JsonArray
import jodd.util.Wildcard
import sz.scaffold.aop.interceptors.GlobalInterceptorBase
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.errors.builtin.SzErrors

// app.httpServer.interceptors 中的配置
// excludes 采用通配符匹配
// Possible patterns allow to match single characters ('?') or any count of characters ('*').
// Wildcard characters can be escaped (by an '\'). When matching path, deep tree wildcard also can be used ('**').
//{
//    className = "sz.scaffold.aop.interceptors.builtin.GlobalQpsLimiter"
//    config = {
//        qps = 150
//        excludes = []
//    }
//}

class GlobalQpsLimiter : GlobalInterceptorBase() {

    override suspend fun call(): Any? {
        val excludes = this.config.getJsonArray("excludes", JsonArray()).map { it.toString() }
        val path = this.httpContext.request().path()
        if (excludes.any { Wildcard.match(path, it) }) {
            return delegate.call()
        }

        val apiLimiter = QpsLimiterMap.getLimiterOrNull(path)
        val usedLimiter = if (apiLimiter != null) apiLimiter else QpsLimiterMap.globalQpsLimiter!!
        return if (usedLimiter.tryAcquire()) {
            delegate.call()
        } else {
            val reply = ReplyBase()
            reply.ret = SzErrors.ExceedQpsLimit.code
            reply.errmsg = "${SzErrors.ExceedQpsLimit.desc} [max ${usedLimiter.rate} 次/秒]"
            reply
        }
    }

}