package sz.scaffold.cache.redis

import com.github.benmanes.caffeine.cache.Cache
import redis.clients.jedis.JedisPubSub
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import sz.scaffold.tools.logger.colorDebug

//
// Created by kk on 2018/10/18.
//
class L2CachePubSub(private val jedisPool: JRedisPool, private val localCache: Cache<String, String?>) : JedisPubSub() {

    private val logger = Logger.of("JRedisL2Cache")

    override fun onPSubscribe(pattern: String?, subscribedChannels: Int) {
        logger.colorDebug("onPSubscribe: [pattern: $pattern] [subscribedChannels: $subscribedChannels]")
    }

    override fun onPMessage(pattern: String, channel: String, message: String) {
        logger.colorDebug("onPMessage: [pattern: $pattern] [channel: $channel] [message: $message]")
        val key = channel.split("__:").last()
        when (message) {
            in setOf("expired", "del", "evicted") -> {
                logger.colorDebug("Key: '$key' 在 redis server 中被 $message, remove it from local level 2 cache", AnsiColor.YELLOW)
                localCache.invalidate(key)
            }
            else -> {
                // reload from Redis
                jedisPool.jedis().use { jedis ->
                    try {
                        val value = jedis.get(key)
                        if (value == null) {
                            localCache.invalidate(key)
                        } else if (localCache.asMap().containsKey(key)) {
                            logger.colorDebug("Key: '$key' 在 redis server 中被更新, 所以同步更新本地缓存. new value: '$value'", AnsiColor.YELLOW)
                            localCache.put(key, value)
                        }
                    } catch (ex: Exception) {
                        // 获取最新的出现错误, 为了避免脏数据, 所以把 local cache 中的也删除掉
                        localCache.invalidate(key)
                    }
                }
            }
        }
    }


}