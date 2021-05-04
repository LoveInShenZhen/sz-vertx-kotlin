package sz.cache.interceptors

//
// Created by kk on 2019-06-28.
//

import io.vertx.core.http.HttpMethod
import jodd.crypt.DigestEngine
import sz.scaffold.Application
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.cache.CacheManager
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.json.toShortJson

@WithAction(ApiCacheAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiCache(
    val expireTimeInMs: Long,
    val excludeQueryParams: Array<String> = ["_"]
)

class ApiCacheAction : Action<ApiCache>() {

    override suspend fun call(): Any? {
        val request = this.httpContext.request()
        val excludes = this.config.excludeQueryParams.toSet()
        val queryParamsTxt = request.params()
            .filter { excludes.contains(it.key).not() }.joinToString("&") { "${it.key}=${it.value}" }

        val bodyParams = if (request.method() == HttpMethod.POST) {
            this.httpContext.bodyAsString + request.formAttributes().joinToString("&") { "${it.key}=${it.value}" }
        } else {
            ""
        }

        val cacheKey = "ApiCache@${request.path()}@${request.method().name()}@${DigestEngine.sha1().digestString(queryParamsTxt + bodyParams)}"
        val cache = CacheManager.asyncCache(cacheName)
        val cacheValue = cache.getOrNullAwait(cacheKey)

        return if (cacheValue == null) {
            val result = delegate.call()
            if (result != null) {
                cache.setAwait(cacheKey, result.toShortJson(), this.config.expireTimeInMs)
            }
            result
        } else {
            val response = this.httpContext.response()
            response.putHeader("Content-Type", ContentTypes.Json)
            response.write(cacheValue)
            null
        }
    }

    companion object {
        private val cacheName = Application.config.getString("app.httpServer.apiCacheName")
    }
}
