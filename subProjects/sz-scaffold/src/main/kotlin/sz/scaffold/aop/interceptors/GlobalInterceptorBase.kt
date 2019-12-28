package sz.scaffold.aop.interceptors

import io.vertx.core.json.JsonObject
import jodd.util.Wildcard
import sz.scaffold.aop.actions.Action

//
// Created by kk on 2019-06-26.
//
abstract class GlobalInterceptorBase : Action<JsonObject>() {

    private val includes = config.getJsonArray("includes")
    private val excludes = config.getJsonArray("excludes")

    protected open fun match(path: String): Boolean {
        return includes.any { Wildcard.match(path, it.toString()) } &&
            excludes.any { Wildcard.match(path, it.toString()) }.not()
    }

    abstract suspend fun whenMatch(): Any?

    override suspend fun call(): Any? {
        val path = this.httpContext.request().path()
        return if (match(path)) {
            whenMatch()
        } else {
            delegate.call()
        }
    }
}