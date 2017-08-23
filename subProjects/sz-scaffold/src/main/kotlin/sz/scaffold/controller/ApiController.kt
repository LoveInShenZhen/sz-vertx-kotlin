package sz.scaffold.controller

import io.vertx.ext.web.RoutingContext

//
// Created by kk on 17/8/16.
//
open class ApiController {

    private var _context: RoutingContext? = null

    fun setupContext(context: RoutingContext) {
        _context = context
    }

    val httpContext: RoutingContext
        get() {
            if (_context == null) {
                throw RuntimeException("httpContext 没有被初始化")
            }
            return _context!!
        }

    fun contentType(contentType: String) {
        httpContext.response().putHeader("Content-Type", contentType)
    }
}