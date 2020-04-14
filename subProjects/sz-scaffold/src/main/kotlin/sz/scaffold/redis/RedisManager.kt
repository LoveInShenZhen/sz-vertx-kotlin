package sz.scaffold.redis

import io.vertx.core.json.JsonObject
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisOptions
import sz.scaffold.Application
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toShortJson

//
// Created by kk on 2020/4/14.
//
object RedisManager {

    private val instences = mutableMapOf<String, Redis>()

    fun redisOf(name: String): Redis {
        return instences[name] ?: createRedis(name)
    }

    @Synchronized
    private fun createRedis(name: String): Redis {
        return instences.getOrPut(name) {
            Redis.createClient(Application.vertx, RedisOptions(optionsOf(name)))
        }
    }

    private fun optionsOf(name: String): JsonObject {
        val cfgPath = "redis.$name"
        if (Application.config.hasPath(cfgPath)) {
            return JsonObject(Application.config.getConfig(cfgPath).root().unwrapped().toShortJson())
        } else {
            throw SzException("Please check application.conf, the name of redis: [$name] does not exists.")
        }
    }
}