package sz.scaffold.aop.interceptors.builtin

//
// Created by kk on 2019-06-28.
//

import io.vertx.core.http.HttpMethod
import jodd.crypt.DigestEngine
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.cache.redis.RedisCacheApi
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.json.toShortJson

@WithAction(ApiCacheAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiCache(
    val expireTimeInMs: Long,
    val cacheName: String = "default",
    val excludeQueryParams: Array<String> = arrayOf("_")
)

class ApiCacheAction : Action<ApiCache>() {

    override suspend fun call(): Any? {
        val request = this.httpContext.request()
        val excludes = this.config.excludeQueryParams.toSet()
        val queryParamsTxt = request.params()
            .filter { excludes.contains(it.key).not() }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")

        val bodyParams = if (request.method() == HttpMethod.POST) {
            this.httpContext.bodyAsString + request.formAttributes().joinToString("&") { "${it.key}=${it.value}" }
        } else {
            ""
        }

        val cacheKey = "ApiCache@${request.path()}@${request.method().name}@${DigestEngine.sha1().digestString(queryParamsTxt + bodyParams)}"
        val cache = RedisCacheApi(this.config.cacheName)
        val cacheValue = cache.getOrNullAwait(cacheKey)

        if (cacheValue == null) {
            val result = delegate.call()
            result?.let {
                cache.setAwait(cacheKey, it.toShortJson(), this.config.expireTimeInMs)
            }
            return result
        } else {
            val response = this.httpContext.response()
            response.putHeader("Content-Type", ContentTypes.Json)
            response.write(cacheValue)
            return null
        }
    }
}
