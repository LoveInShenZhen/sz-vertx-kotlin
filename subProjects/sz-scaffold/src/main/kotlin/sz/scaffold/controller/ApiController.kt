package sz.scaffold.controller

import io.vertx.ext.web.RoutingContext
import jodd.http.HttpUtil
import java.net.URLDecoder

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

    fun contentCharset(): String {
        if (this.httpContext.request().headers().contains("Content-Type")) {
            val charset = HttpUtil.extractContentTypeCharset(this.httpContext.request().headers().get("Content-Type"))
            if (charset.isNullOrBlank()) {
                return "UTF-8"
            } else {
                return charset
            }
        } else {
            return "UTF-8"
        }
    }

    fun formFields(needDecode: Boolean = false, enc: String = "UTF-8"): Map<String, String> {
        val bodyStr = this.httpContext.getBodyAsString(contentCharset())
        val form = HttpUtil.parseQuery(bodyStr, false)
        if (needDecode) {
            return form.map { Pair<String, String>(it.key, URLDecoder.decode(it.value, enc)) }.toMap()
        } else {
            return form.map { Pair<String, String>(it.key, it.value) }.toMap()
        }

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
        val forwardIp = this.httpContext.request().getHeader("X-Forwarded-For")

        if (realIp.isNotBlank()) {
            return realIp
        }

        if (forwardIp.isNotBlank()) {
            return forwardIp
        }

        return this.httpContext.request().remoteAddress().toString()
    }
}