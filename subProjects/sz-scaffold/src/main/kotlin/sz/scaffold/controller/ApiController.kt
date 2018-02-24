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

    fun redirect(newLocation: String) {
        httpContext.response().statusCode = 307
        httpContext.response().putHeader("Location", newLocation).end()
    }

    //    Nginx 配置:
    //
    //    proxy_http_version 1.1;
    //    proxy_set_header   Host             $http_host;
    //    proxy_set_header   X-Real-IP        $remote_addr;
    //    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;

    fun ClientIp(): String {
        val realIp = this.httpContext.request().getHeader("X-Real-IP")
        if (realIp.isNullOrBlank()) {
            return this.httpContext.request().remoteAddress().toString()
        } else {
            return realIp
        }
    }
}