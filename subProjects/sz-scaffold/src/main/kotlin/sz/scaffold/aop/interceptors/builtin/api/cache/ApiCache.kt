package sz.scaffold.aop.interceptors.builtin.api.cache

//
// Created by kk on 2019-06-28.
//

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.vertx.core.http.HttpMethod
import jodd.crypt.DigestEngine
import sz.scaffold.Application
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.json.toShortJson
import java.util.concurrent.TimeUnit

@WithAction(ApiCacheAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiCache(
    val expireTimeInSeconds: Int,
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

        val cacheKey = "ApiCache@${request.path()}@${request.method().name}@${DigestEngine.sha1().digestString(queryParamsTxt + bodyParams)}"
        val cache = ApiTimeBaseCaches.cacheOf(this.config.expireTimeInSeconds)
        val cacheValue = cache.getIfPresent(cacheKey)

        return if (cacheValue == null) {
            val result = delegate.call()
            if (result != null) {
                cache.put(cacheKey, result.toShortJson())
            }
            result
        } else {
            val response = this.httpContext.response()
            response.putHeader("Content-Type", ContentTypes.Json)
            response.write(cacheValue)
            null
        }
    }
}

internal object ApiTimeBaseCaches {

    private val instences = mutableMapOf<Int, Cache<String, String>>()

    fun cacheOf(expireTimeInSeconds: Int): Cache<String, String> {
        return instences[expireTimeInSeconds] ?: createCache(expireTimeInSeconds)
    }

    @Synchronized
    private fun createCache(expireTimeInSeconds: Int): Cache<String, String> {
        return instences.getOrPut(expireTimeInSeconds) {
            CacheBuilder.newBuilder()
                .maximumSize(Application.config.getLong("app.httpServer.apiCache.maximumSize"))
                .expireAfterWrite(expireTimeInSeconds.toLong(), TimeUnit.SECONDS)
                .build()
        }
    }
}
