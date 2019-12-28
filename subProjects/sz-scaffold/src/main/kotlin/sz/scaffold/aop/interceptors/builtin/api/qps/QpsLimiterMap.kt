package sz.scaffold.aop.interceptors.builtin.api.qps

import com.google.common.util.concurrent.RateLimiter
import sz.scaffold.Application
import sz.scaffold.tools.SzException
import kotlin.reflect.full.findAnnotation

/**
 * 限流器Map, key 为 route path, value 为限流器:RateLimiter 实例
 */
@Suppress("UnstableApiUsage")
object QpsLimiterMap {
    // key: route path
    private val apiRateLimiterMap: Map<String, RateLimiter>

    // key: name of limter
    private val namedRateLimiters: Map<String, RateLimiter>

    init {
        apiRateLimiterMap = apiRateLimiterMapByConfig()
        namedRateLimiters = globalRateLimitersByConfig()
    }

    private fun apiRateLimiterMapByConfig(): Map<String, RateLimiter> {
        val map = mutableMapOf<String, RateLimiter>()
        Application.loadApiRouteFromRouteFiles().forEach { apiRoute ->
            val annQpsLimiter = apiRoute.controllerFun.findAnnotation<QpsLimiter>()
            if (annQpsLimiter != null) {
                val limiter = RateLimiter.create(annQpsLimiter.qps)
                map[apiRoute.path] = limiter
            }
        }
        return map
    }

    //{
    //    className = "sz.scaffold.aop.interceptors.builtin.api.qps.GlobalQpsLimiter"
    //    config = {
    //        name = "nameOfLimiter"
    //        qps = 150
    //        includes = ["/**"]
    //        excludes = []
    //    }
    //}
    private fun globalRateLimitersByConfig(): Map<String, RateLimiter> {
        return Application.config.getConfigList("app.httpServer.interceptors").filter {
            it.getString("className") == GlobalQpsLimiter::class.java.name
        }.map {
            val rateLimiter = RateLimiter.create(it.getDouble("config.qps"))
            val name = it.getString("config.name")
            Pair(name, rateLimiter)
        }.toMap()
    }

    fun apiLimiterOf(routePath: String): RateLimiter {
        return apiRateLimiterMap.getOrElse(routePath) {
            throw SzException("No rateLimiter found for path: $routePath")
        }
    }

    fun namedLimiterOf(name: String): RateLimiter {
        return namedRateLimiters.getOrElse(name) {
            throw SzException("No rateLimiter found for name: $name")
        }
    }
}