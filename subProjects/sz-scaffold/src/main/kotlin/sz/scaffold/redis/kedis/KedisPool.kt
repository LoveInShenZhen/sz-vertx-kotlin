package sz.scaffold.redis.kedis

import com.typesafe.config.Config
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.redis.client.redisOptionsOf
import io.vertx.redis.client.RedisClientType
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.RedisRole
import sz.crypto.RsaUtil
import sz.objectPool.ObjectPool
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getStringOrElse
import sz.scaffold.tools.SzException
import java.io.File

//
// Created by kk on 2019-06-11.
//
class KedisPool(vertx: Vertx,
                val redisOptions: RedisOptions,
                val poolConfig: KedisPoolConfig
) : ObjectPool<KedisAPI>(poolConfig.objectPoolConfig, KedisAPIPooledObjectFactory(vertx, redisOptions, poolConfig.operationTimeout)) {

    companion object {

        private val pools = mutableMapOf<String, KedisPool>()

        private val globalConfig = Application.config.getConfig("redis.globalConfig")

        private val serversConfig = Application.config.getConfig("redis.servers")

        private val encryptPasswd = globalConfig.getBoolean("encryptPasswd")

        private val privateKey by lazy {
            val privateKeyFile = File(globalConfig.getString("privateKeyFile"))
            RsaUtil.privateKeyFromPem(privateKeyFile.readText())
        }


        init {
            initPool()
        }

        fun initPool() {
            serversConfig.root().keys.forEach { serverName ->
                byName(serverName)
            }
        }

        private fun defaultPoolConfig(): KedisPoolConfig {
            return KedisPoolConfig.buildFrom(serversConfig.getConfig("default.pool"))
        }

        private fun createKedisPoolByName(name: String): KedisPool {
            if (serversConfig.hasPath(name).not()) {
                throw SzException("Please check application.conf, there is no config path for 'redis.servers.$name'")
            }
            val redisOptions = redisOptionsByName(name)
            val poolConfig = if (serversConfig.hasPath("$name.pool")) {
                KedisPoolConfig.buildFrom(serversConfig.getConfig("$name.pool"))
            } else {
                defaultPoolConfig()
            }

            return KedisPool(vertx = Application.vertx, redisOptions = redisOptions, poolConfig = poolConfig)
        }

        private fun redisOptionsByName(name: String): RedisOptions {
            val config = serversConfig.getConfig(name)
            return when (config.getStringOrElse("workingMode", "STANDALONE")) {
                "STANDALONE" -> redisOptionsOf(type = RedisClientType.STANDALONE,
                    endpoint = SocketAddress.inetSocketAddress(config.getIntOrElse("port", 6379), config.getString("host")),
                    netClientOptions = createNetclientOptions(name),
                    password = config.getPassword("password"),
                    select = config.getIntOrElse("database", 0))

                "SENTINEL" -> redisOptionsOf(type = RedisClientType.SENTINEL,
                    endpoints = config.getStringList("servers").map { endpointOf(it) },
                    netClientOptions = createNetclientOptions(name),
                    password = config.getPassword("password"),
                    select = config.getIntOrElse("database", 0),
                    masterName = config.getString("masterName"),
                    role = RedisRole.MASTER)

                "CLUSTER" -> redisOptionsOf(type = RedisClientType.CLUSTER,
                    endpoints = config.getStringList("servers").map { endpointOf(it) },
                    netClientOptions = createNetclientOptions(name),
                    password = config.getPassword("password"),
                    select = config.getIntOrElse("database", 0))

                else -> throw SzException("无效的 redis workingMode 设置")
            }
        }

        private fun createNetclientOptions(name: String): NetClientOptions {
            val configPath = "$name.netClientOptions"
            val config = if (serversConfig.hasPath(configPath)) {
                serversConfig.getConfig(configPath)
            } else {
                // 由 name 指定的 server 没有特别设置 netClientOptions, 就以默认 server 的 netClientOptions 配置为准
                serversConfig.getConfig("default.netClientOptions")
            }
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
            // 空字符串, 代表默认的 Redis Server
            if (name.isBlank()) return true
            return serversConfig.hasPath(name)
        }

        private fun Config.getStringEmptyAsNull(path: String): String? {
            return if (this.hasPath(path)) {
                val value = this.getString(path)
                if (value.isEmpty()) {
                    null
                } else {
                    value
                }
            } else {
                // 未配置该项, 返回 null
                null
            }
        }

        private fun Config.getPassword(path: String): String? {
            val pwd = this.getStringEmptyAsNull(path)
            return if (encryptPasswd) {
                // 密码为密文
                if (pwd.isNullOrBlank()) {
                    // 密码为空
                    null
                } else {
                    // 解密, 还原成明文
                    RsaUtil.decrypt(pwd, privateKey)
                }
            } else {
                // 密码为明文
                pwd
            }
        }
    }
}