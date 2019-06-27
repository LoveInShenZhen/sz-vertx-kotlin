package sz.scaffold.aop.interceptors.builtin

//
// Created by kk on 2019-06-27.
//
import jodd.crypt.DigestEngine
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.ContentTypes
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger

@WithAction(ApiETagAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiETag

class ApiETagAction : Action<ApiETag>() {

    override suspend fun call(): Any? {
        val result = delegate.call()
        if (result is ReplyBase) {
            val response = this.httpContext.response()
            val currentEtag = DigestEngine.sha1().digestString(result.toShortJson())
            response.putHeader("ETag", currentEtag)
            response.putHeader("Cache-Control", "no-cache")

            val reqEtag = etagOfRequest()

            return if (currentEtag == reqEtag) {
                // 304
                response.statusCode = 304
                response.putHeader("Content-Type", ContentTypes.Json)
                Logger.debug("ETAG 检查,内容未发生变化, 返回 304")
                null
            } else {
                result
            }
        } else {
            return result
        }
    }

    private fun etagOfRequest():String {
        val headers = this.httpContext.request().headers()
        if (headers.contains("If-None-Match")) {
            return headers.get("If-None-Match")
        }
        if (headers.contains("If-Match")) {
            return headers.get("If-Match")
        }

        return ""
    }
}