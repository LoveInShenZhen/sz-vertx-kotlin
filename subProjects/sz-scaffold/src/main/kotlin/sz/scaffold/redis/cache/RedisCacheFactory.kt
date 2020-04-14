package sz.scaffold.redis.cache

import io.vertx.core.json.JsonObject
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisOptions
import sz.scaffold.Application
import sz.scaffold.cache.AsyncCacheApi
import sz.scaffold.cache.CacheApi
import sz.scaffold.cache.CacheFactory

//
// Created by kk on 2020/4/13.
//
class RedisCacheFactory: CacheFactory {
    override fun createCache(options: JsonObject): CacheApi {
        val redis = Redis.createClient(Application.vertx, RedisOptions(options))
        return RedisCache(redis)
    }

    override fun createAsyncCache(options: JsonObject): AsyncCacheApi {
        val redis = Redis.createClient(Application.vertx, RedisOptions(options))
        return RedisAsyncCache(redis)
    }
}