package sz.scaffold.cache.redis

import sz.scaffold.Application
import sz.scaffold.cache.CacheApi
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/4.
//
class RedisCacheApi(val name:String = "default", private val cacheImp : CacheApi = createCacheApi(name)) : CacheApi by cacheImp {

    companion object {

        private val cacheInstances = mutableMapOf<String, CacheApi>()
        private val logger = Logger.of("JRedisL2Cache")

        private fun createCacheApi(redisName:String = "default") : CacheApi {
            return cacheInstances.getOrElse(redisName) {
                val l2CacheEnabled = Application.config.getBooleanOrElse("redis.$redisName.level2.cacheEnabled", false)

                val cacheImp = if (l2CacheEnabled) {
                    logger.debug("Redis Cache, name: $redisName, class: JRedisL2Cache (本地2级缓存)")
                    JRedisL2Cache(JRedisPool.byName(redisName)).start()
                } else {
                    logger.debug("Redis Cache, name: $redisName, class: RedisCacheApiImp")
                    RedisCacheApiImp(redisName)
                }
                cacheInstances.putIfAbsent(redisName, cacheImp)
                cacheImp
            }



        }

    }
}