package sz.cache.local

import com.google.common.cache.CacheBuilder
import io.vertx.core.json.JsonObject
import sz.cache.AsyncCacheApi
import sz.cache.CacheApi
import sz.cache.CacheFactory

//
// Created by kk on 2020/4/16.
//
class LocalCacheFactory : CacheFactory {
    override fun createCache(options: JsonObject): CacheApi {
        val maximumSize = options.getLong("maximumSize") ?: 2048
        return LocalCache(CacheBuilder.newBuilder().maximumSize(maximumSize).build())
    }

    override fun createAsyncCache(options: JsonObject): AsyncCacheApi {
        val maximumSize = options.getLong("maximumSize") ?: 2048
        return LocalAsyncCache(CacheBuilder.newBuilder().maximumSize(maximumSize).build())
    }
}