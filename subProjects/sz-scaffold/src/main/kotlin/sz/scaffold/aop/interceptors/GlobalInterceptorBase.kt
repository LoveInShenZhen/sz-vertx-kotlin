package sz.scaffold.aop.interceptors

import io.vertx.core.json.JsonObject
import jodd.util.Wildcard
import sz.scaffold.aop.actions.Action

//
// Created by kk on 2019-06-26.
//
abstract class GlobalInterceptorBase : Action<JsonObject>() {

    private val includes: List<String>
        get() {
            val includesCfg = config.getJsonArray("includes").map { it.toString() }.toMutableList()
            return if (includesCfg.isEmpty()) {
                // default include all route path
                listOf("/**")
            } else {
                includesCfg.toList()
            }
        }

    private val excludes: List<String>
        get() {
            return config.getJsonArray("excludes").map { it.toString() }
        }

    protected open fun match(path: String): Boolean {
        return includes.any { Wildcard.match(path, it) } &&
            excludes.any { Wildcard.match(path, it) }.not()
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