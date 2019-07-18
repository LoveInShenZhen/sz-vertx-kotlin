package sz.scaffold.redis.kedis.pool

import com.typesafe.config.Config
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.core.net.netClientOptionsOf
import io.vertx.kotlin.redis.client.redisOptionsOf
import io.vertx.redis.client.RedisClientType
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.RedisRole
import jodd.system.SystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getStringOrElse
import sz.scaffold.redis.kedis.KedisPoolConfig
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

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
            val redisOptions = redisOptionsByName(name)
            val poolConfig = if (Application.config.hasPath("redis.$name.pool")) {
                KedisPoolConfig.buildFrom(Application.config.getConfig("redis.$name.pool"))
            } else {
                defaultPoolConfig()
            }

            return KedisPool(vertx = Application.vertx, redisOptions = redisOptions, poolConfig = poolConfig)
        }

        private fun redisOptionsByName(name: String): RedisOptions {
            val config = Application.config.getConfig("redis.$name")
            return when (config.getStringOrElse("workingMode", "STANDALONE")) {
                "STANDALONE" -> redisOptionsOf(type = RedisClientType.STANDALONE,
                    endpoint = SocketAddress.inetSocketAddress(config.getIntOrElse("port", 6379), config.getString("host")),
                    netClientOptions = createNetclientOptions(name),
                    password = config.getStringEmptyAsNull("password"),
                    select = config.getIntOrElse("database", 0))

                "SENTINEL" -> redisOptionsOf(type = RedisClientType.SENTINEL,
                    endpoints = config.getStringList("servers").map { endpointOf(it) },
                    netClientOptions = createNetclientOptions(name),
                    masterName = config.getString("masterName"),
                    role = RedisRole.MASTER)

                "CLUSTER" -> redisOptionsOf(type = RedisClientType.CLUSTER,
                    endpoints = config.getStringList("servers").map { endpointOf(it) },
                    netClientOptions = createNetclientOptions(name))

                else -> throw SzException("无效的 redis workingMode 设置")
            }
        }

        private fun createNetclientOptions(name: String): NetClientOptions {
            val configPath = "redis.$name.netClientOptions"
            val config = if (Application.config.hasPath(configPath)) {
                Application.config.getConfig(configPath)
            } else {
                Application.config.getConfig("redis.default.netClientOptions")
            }
//            Logger.debug("redis net client options:\n${JsonObject(config.root().unwrapped()).toJsonPretty()}")
            return NetClientOptions(JsonObject(config.root().unwrapped()))
        }

        private fun endpointOf(server: String): SocketAddress {
            val parts = server.split(":")
            return SocketAddress.inetSocketAddress(parts[1].toInt(), parts[0])
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

        fun exists(name: String): Boolean {
            if (name.isBlank()) return true
            return Application.config.hasPath("redis.$name")
        }

        private fun Config.getStringEmptyAsNull(path: String): String? {
            if (this.hasPath(path)) {
                val value = this.getString(path)
                if (value.isEmpty()) {
                    return null
                } else {
                    return value
                }
            } else {
                return null
            }
        }
    }
}