package sz.scaffold.cache.redis

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import jodd.datetime.JDateTime
import redis.clients.jedis.Response
import sz.scaffold.Application
import sz.scaffold.cache.CacheApi
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

//
// Created by kk on 2018/10/17.
//
class JRedisL2Cache(private val jedisPool: JRedisPool,
                    private val localCache: LoadingCache<String, String?> = createLocalCache(jedisPool),
                    private val executor: Executor = ForkJoinPool.commonPool()) : CacheApi {

    private val pubSub = L2CachePubSub(jedisPool, localCache)
    private val psubPattern = "__keyspace@${jedisPool.database}__:*"
    private val logger = Logger.of("JRedisL2Cache")

    private var enabled = false

    fun start(): JRedisL2Cache {
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

            val autoRefresh = Application.config.getLong("redis.${jedisPool.name}.level2.autoRefresh")
            if (autoRefresh > 0) {
                object : Thread() {
                    override fun run() {
                        logger.debug("Auto refresh local cache thread started. autoRefresh time: $autoRefresh Seconds")
                        while (enabled) {
                            Thread.sleep(autoRefresh * 1000)

                            jedisPool.jedis().use { jedis ->
                                val piple = jedis.pipelined()
                                val refreshedDatas = localCache.asMap().map { entry ->
                                    Pair<String, Response<String?>>(entry.key, piple.get(entry.key))
                                }
                                piple.sync()
                                refreshedDatas.forEach {
                                    val newValue = it.second.get()
                                    if (newValue == null) {
                                        localCache.invalidate(it.first)
                                        logger.debug("Auto refresh remove for key: '${it.first}'")
                                    } else {
                                        localCache.put(it.first, newValue)
                                        logger.debug("Auto refresh update for key: '${it.first}', latest value: '$newValue'")
                                    }
                                }
                            }
                        }
                        logger.debug("stop auto refresh local cache")
                    }
                }.start()
            }
        }

        return this
    }

    fun stop() {
        if (enabled) {
            pubSub.punsubscribe(psubPattern)
            enabled = false
        }
    }

    override fun exists(key: String): Boolean {
        return localCache.asMap().containsKey(key)
    }

    override fun get(key: String): String {
        return localCache.get(key) ?: throw SzException("$key 在缓存中不存在")
    }

    override fun getOrElse(key: String, default: () -> String): String {
        return localCache.get(key) ?: default()
    }

    override fun getOrNull(key: String): String? {
        return localCache.get(key)
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
                    logger.debug("Set key: '$key' to value: '$objJson' on redis server.")
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
                logger.debug("Set key: '$key' to value: '$objJson' on redis server.")
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

        private val logger = Logger.of("JRedisL2Cache")

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

        fun createLocalCache(jedisPool: JRedisPool): LoadingCache<String, String?> {
            val expireAfterAccess = Application.config.getLong("redis.${jedisPool.name}.level2.expireAfterAccess")
            val expireAfterWrite = Application.config.getLong("redis.${jedisPool.name}.level2.expireAfterWrite")

            val builder = Caffeine.newBuilder()

            if (expireAfterAccess > 0) {
                builder.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS)
            }

            if (expireAfterWrite > 0) {
                builder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS)
            }

//            builder.removalListener<String, String?> { key, value, cause -> logger.debug("Remove key: '$key', value: '$value', cause: $cause") }

            return builder.build<String, String?> { key: String ->
                jedisPool.jedis().use {
                    val value = it.get(key)
                    logger.debug("Get value for key: '$key' from redis server:\n$value")
                    value
                }
            }
        }
    }


}