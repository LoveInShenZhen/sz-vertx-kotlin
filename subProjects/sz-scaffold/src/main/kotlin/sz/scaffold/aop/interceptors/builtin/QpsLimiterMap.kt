package sz.scaffold.aop.interceptors.builtin

import com.google.common.util.concurrent.RateLimiter
import sz.scaffold.Application
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import kotlin.reflect.full.findAnnotation

/**
 * 限流器Map, key 为 route path, value 为限流器:RateLimiter 实例
 */
@Suppress("UnstableApiUsage")
object QpsLimiterMap {
    private val map = mutableMapOf<String, RateLimiter>()

    // 全局限流器应该只有一个, 选择最后一个有效
    val globalQpsLimiter: RateLimiter? by lazy {
        val cfg = Application.config.getConfigList("app.httpServer.interceptors").filter {
            it.getString("className") == GlobalQpsLimiter::class.java.name
        }.lastOrNull()
        Logger.debug("====>\n${cfg?.root()?.unwrapped()?.toJsonPretty()}")
        if (cfg == null) {
            return@lazy null
        } else {
            return@lazy RateLimiter.create(cfg.getDouble("config.qps"))
        }
    }

    init {
        initMapByConfig()
    }

    private fun initMapByConfig() {
        Application.loadApiRouteFromRouteFiles().forEach { apiRoute ->
            val annQpsLimiter = apiRoute.controllerFun.findAnnotation<QpsLimiter>()
            if (annQpsLimiter != null) {
                val limiter = RateLimiter.create(annQpsLimiter.qps)
                map[apiRoute.path] = limiter
            }
        }
    }

    fun limiterOf(routePath: String): RateLimiter {
        return map[routePath]!!
    }

    fun getLimiterOrNull(routePath: String): RateLimiter? {
        return map[routePath]
    }
}