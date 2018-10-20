package sz.scaffold.cache.redis

import com.github.benmanes.caffeine.cache.Cache
import jodd.datetime.JDateTime
import sz.scaffold.cache.CacheApi
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

//
// Created by kk on 2018/10/17.
//
class JRedisL2Cache(private val jedisPool: JRedisPool,
                    private val localCache: Cache<String, String>,
                    private val executor: Executor = ForkJoinPool.commonPool()) : CacheApi {

    private val pubSub = L2CachePubSub(jedisPool, localCache)
    private val psubPattern = "__keyspace@${jedisPool.database}__:*"

    private var enabled = false

    fun makeItWork() {
        if (enabled.not()) {
            enableKeySpaceNotify(jedisPool)

            object : Thread() {
                override fun run() {
                    jedisPool.jedis().use { jedis ->
                        jedis.psubscribe(pubSub, psubPattern)
                    }
                }
            }.start()


            enabled = true
        }
    }

    override fun exists(key: String): Boolean {
        return localCache.asMap().containsKey(key)
    }

    override fun get(key: String): String {
        return localCache.get(key) {
            jedisPool.jedis().use { jedis ->
                jedis.get(key) ?: throw SzException("$key 在缓存中不存在")
            }
        }!!
    }

    override fun getOrElse(key: String, default: () -> String): String {
        return localCache.get(key) {
            jedisPool.jedis().use { jedis ->
                jedis.get(key) ?: default()
            }
        }!!
    }

    override fun getOrNull(key: String): String? {
        return localCache.get(key) {
            jedisPool.jedis().use { jedis ->
                jedis.get(key)
            }
        }
    }

    override fun set(key: String, objJson: String, expirationInMs: Long) {
        localCache.put(key, objJson)
        executor.execute {
            try {
                jedisPool.jedis().use {
                    if (expirationInMs > 0) {
                        it.psetex(key, expirationInMs, objJson)
                    } else {
                        it.set(key, objJson)
                    }
                }
            } catch (ex: Exception) {
                Logger.error(ex.localizedMessage)
            }
        }
    }

    override fun set(key: String, objJson: String) {
        localCache.put(key, objJson)
        executor.execute {
            try {
                jedisPool.jedis().use {
                    it.set(key, objJson)
                }
            } catch (ex: Exception) {
                Logger.error(ex.localizedMessage)
            }
        }
    }

    override fun set(key: String, objJson: String, cleaningTime: JDateTime) {
        val now = JDateTime().convertToDate().time
        val diffTime = cleaningTime.convertToDate().time - now
        if (diffTime <= 0) {
            set(key, objJson)
        } else {
            set(key, objJson, diffTime)
        }
    }

    override fun del(key: String) {
        localCache.invalidate(key)
        executor.execute {
            try {
                jedisPool.jedis().use {
                    it.del(key)
                }
            } catch (ex: Exception) {
                Logger.error(ex.localizedMessage)
            }
        }
    }

    companion object {

//        /**
//         * 因为开启 键事件频道 通知功能需要消耗一些 CPU ， 所以在默认配置下， 该功能处于关闭状态
//         * 参考: http://redisdoc.com/topic/notification.html
//         */
//        fun enableKeyEventsNotify(jedisPool: JRedisPool) {
//            jedisPool.jedis().use {
//                val config = it.configGet("notify-keyspace-events")
//                if (config[1].contains("E").not()) {
//                    it.configSet("notify-keyspace-events", "E${config[1]}")
//                }
//            }
//        }
//
//        /**
//         * 关闭 键事件频道 通知功能
//         */
//        fun disableKeyEventsNotify(jedisPool: JRedisPool) {
//            jedisPool.jedis().use {
//                val config = it.configGet("notify-keyspace-events")
//                if (config[1].contains("E")) {
//                    it.configSet("notify-keyspace-events", config[1].replace("E", ""))
//                }
//            }
//        }

        /**
         * 因为开启 键空间频道 通知功能需要消耗一些 CPU ， 所以在默认配置下， 该功能处于关闭状态
         * 参考: http://redisdoc.com/topic/notification.html
         */
        fun enableKeySpaceNotify(jedisPool: JRedisPool) {
            jedisPool.jedis().use {
                val config = it.configGet("notify-keyspace-events")
                if (config[1].contains("K").not() && config[1].contains("A").not()) {
                    it.configSet("notify-keyspace-events", "AK")
                }
            }
        }

//        /**
//         * 关闭 键空间频道 通知功能
//         */
//        fun disableKeySpaceNotify(jedisPool: JRedisPool) {
//            jedisPool.jedis().use {
//                val config = it.configGet("notify-keyspace-events")
//                if (config[1].contains("K").not()) {
//                    it.configSet("notify-keyspace-events", config[1].replace("K", ""))
//                }
//            }
//        }
    }


}