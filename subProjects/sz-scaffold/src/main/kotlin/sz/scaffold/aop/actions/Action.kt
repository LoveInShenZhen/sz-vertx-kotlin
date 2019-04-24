package sz.scaffold.aop.actions

import io.vertx.ext.web.RoutingContext

//
// Created by kk on 17/8/16.
//
abstract class Action<out T> {

    private var _config: T? = null
    private var _httpContext: RoutingContext? = null
    private var _delegate: Action<*>? = null

    val config: T
        get() = _config!!

    val httpContext: RoutingContext
        get() = _httpContext!!

    val delegate: Action<*>
        get() = _delegate!!

    abstract fun call(): Any?

    @Suppress("UNCHECKED_CAST")
    fun init(conf: Any, context: RoutingContext, delegateAction: Action<*>) {
        this._config = conf as T
        this._httpContext = context
        this._delegate = delegateAction
    }

    fun setupHttpContext(httpContext:RoutingContext) {
        this._httpContext = httpContext
    }

    companion object {

        fun <T> wrapperAction(wrappedMethod: () -> Any?): Action<T> {
            return object : Action<T>() {
                override fun call(): Any? {
                    return wrappedMethod()
                }

            }
        }
    }
}