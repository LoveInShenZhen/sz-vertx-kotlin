package sz.scaffold.controller

import io.vertx.ext.web.RoutingContext
import jodd.bean.BeanCopy
import jodd.http.HttpUtil
import sz.scaffold.tools.json.toJsonNode
import sz.scaffold.tools.json.toObj
import java.net.URLDecoder

//
// Created by kk on 17/8/16.
//

object ContentTypes {
    val Text = "text/plain; charset=utf-8"
    val Html = "text/html; charset=utf-8"
    val Json = "application/json; charset=utf-8"
    val JavaScript = "text/javascript; charset=utf-8"
}

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
        return httpContext.request().formAttributes().map {
            if (needDecode) {
                Pair<String, String>(it.key, URLDecoder.decode(it.value, enc))
            } else {
                Pair<String, String>(it.key,it.value)
            }
        }.toMap()
    }

    inline fun <reified BeanType> postJsonToBean(): BeanType {
        return this.httpContext.getBodyAsString(contentCharset()).toJsonNode().toObj(BeanType::class.java)
    }

    inline fun <reified BeanType> postFormToBean(needDecode: Boolean = false, enc: String = "UTF-8"): BeanType {
        val formMap = formFields(needDecode, enc)
        val bean = BeanType::class.java.newInstance()
        val beanCopy = BeanCopy.fromMap(formMap).toBean(bean)
        beanCopy.copy()
        return bean
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

    fun clientIp(): String {
        val realIp = this.httpContext.request().getHeader("X-Real-IP")
        val forwardIp = this.httpContext.request().getHeader("X-Forwarded-For")

        if (realIp.isNullOrBlank().not()) {
            return realIp
        }

        // 参考: https://help.aliyun.com/knowledge_detail/40535.html?spm=5176.13394938.0.0.1ce64c27rAvyll
        // 参考: https://help.aliyun.com/knowledge_detail/63121.html?spm=a2c4g.11186623.4.5.7ea45005khRtom
        if (forwardIp.isNullOrBlank().not()) {
            return forwardIp.split(",").first().trim()
        }

        return this.httpContext.request().remoteAddress().toString()
    }
}