package sz.scaffold.cache

import io.vertx.core.json.JsonObject

//
// Created by kk on 2020/4/13.
//
interface CacheFactory {

    fun createCache(options: JsonObject): CacheApi

    fun createAsyncCache(options: JsonObject): AsyncCacheApi

}