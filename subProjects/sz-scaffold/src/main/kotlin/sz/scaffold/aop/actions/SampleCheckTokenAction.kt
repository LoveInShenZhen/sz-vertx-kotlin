package sz.scaffold.aop.actions

import sz.scaffold.aop.annotations.SampleCheckToken
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/8/16.
//
class SampleCheckTokenAction : Action<SampleCheckToken>() {

    override fun call(): Any? {
        Logger.debug(" CheckTokenAction.call() ... token: ${this.config.token}, roles: [${this.config.roles.joinToString(",")}]")
//        this.httpContext.response().end("token timeout ...")
//        return Unit
        val reply = ReplyBase()
        reply.ret = -1
        reply.errmsg = "token timeout"
        return reply
//        return delegate.call()
    }
}