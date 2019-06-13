package sz.scaffold.redis.kedis.pool

import sz.scaffold.redis.kedis.KedisPoolConfig
import io.vertx.core.Vertx
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.core.net.netClientOptionsOf
import io.vertx.kotlin.redis.client.redisOptionsOf
import io.vertx.redis.client.RedisOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getStringOrElse
import sz.scaffold.ext.getStringOrNll
import sz.scaffold.tools.SzException

//
// Created by kk on 2019-06-11.
//
class KedisPool(vertx: Vertx, val redisOptions: RedisOptions, val poolConfig: KedisPoolConfig) {

    private val internalPool = KedisAPIPool(factory = KedisAPIPooledObjectFactory(vertx, redisOptions, poolConfig.operationTimeout),
        poolConfig = poolConfig)

    fun borrow(): KedisAPI {
        val kedisApi = internalPool.borrowObject()
        kedisApi.setupCreditor(internalPool)
        return kedisApi
    }

    suspend fun borrowAwait(): KedisAPI {
        return withContext(Dispatchers.Default) {
            val kedisApi = internalPool.borrowObject()
            kedisApi.setupCreditor(internalPool)
            kedisApi
        }
    }

    companion object {

        private val pools = mutableMapOf<String, KedisPool>()

        init {
            initPool()
        }

        fun initPool() {
            val config = Application.config.getConfig("redis")
            config.root().keys.forEach {
                byName(it)
            }
        }

        private fun defaultPoolConfig(): KedisPoolConfig {
            return KedisPoolConfig.buildFrom(Application.config.getConfig("redis.default.pool"))
        }

        private fun createKedisPoolByName(name: String): KedisPool {
            if (Application.config.hasPath("redis.$name").not()) {
                throw SzException("Please check application.conf, there is no config path for 'redis.$name'")
            }
            val config = Application.config.getConfig("redis.$name")
            val redisOptions = redisOptionsOf(endpoint = SocketAddress.inetSocketAddress(config.getIntOrElse("port", 6379), config.getStringOrElse("host", "127.0.0.1")),
                netClientOptions = netClientOptionsOf(connectTimeout = config.getIntOrElse("timeout", 2000)),
                password = config.getStringOrNll("password"),
                select = config.getIntOrElse("database", 0))

            val poolConfig = if (Application.config.hasPath("redis.$name.pool")) {
                KedisPoolConfig.buildFrom(Application.config.getConfig("redis.$name.pool"))
            } else {
                defaultPoolConfig()
            }

            return KedisPool(vertx = Application.vertx, redisOptions = redisOptions, poolConfig = poolConfig)
        }

        fun byName(name: String): KedisPool {
            return pools.getOrPut(name) {
                synchronized(pools) {
                    createKedisPoolByName(name)
                }
            }
        }

        fun default(): KedisPool {
            return byName("default")
        }
    }
}