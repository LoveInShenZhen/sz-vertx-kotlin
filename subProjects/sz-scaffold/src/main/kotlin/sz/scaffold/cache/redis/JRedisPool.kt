package sz.scaffold.cache.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import sz.scaffold.Application
import sz.scaffold.ext.existThenApply
import sz.scaffold.ext.getStringOrNll
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/4.
//
class JRedisPool(val name: String) {

    private var _jedisPool: JedisPool? = null

    private val jedisPool: JedisPool
        get() {
            if (_jedisPool == null) {
                throw SzException("JedisPool 尚未完成初始化")
            }
            return _jedisPool!!
        }

    init {
        _jedisPool = JedisPool(poolConfig(),
                Application.config.getString("redis.$name.host"),
                Application.config.getInt("redis.$name.port"),
                Application.config.getInt("redis.$name.timeout"),
                Application.config.getStringOrNll("redis.$name.password"),
                Application.config.getInt("redis.$name.database"),
                Application.config.getBoolean("redis.$name.ssl")
        )
        Logger.debug("初始化 JedisPool ($name)             [OK]")
    }

    fun pool(): JedisPool {
        return _jedisPool!!
    }

    fun jedis(): Jedis {
        return jedisPool.resource
    }

    private fun poolConfig(): JedisPoolConfig {
        val poolCfg = JedisPoolConfig()
        val config = Application.config
        config.existThenApply("redis.$name.pool.maxIdle") {
            poolCfg.maxIdle = config.getInt("redis.$name.pool.maxIdle")
        }

        config.existThenApply("redis.$name.pool.minIdle") {
            poolCfg.minIdle = config.getInt("redis.$name.pool.minIdle")
        }

        config.existThenApply("redis.$name.pool.maxTotal") {
            poolCfg.maxTotal = config.getInt("redis.$name.pool.maxTotal")
        }

        config.existThenApply("redis.$name.pool.maxWaitMillis") {
            poolCfg.maxWaitMillis = config.getLong("redis.$name.pool.maxWaitMillis")
        }

        config.existThenApply("redis.$name.pool.testOnBorrow") {
            poolCfg.testOnBorrow = config.getBoolean("redis.$name.pool.testOnBorrow")
        }

        config.existThenApply("redis.$name.pool.testOnReturn") {
            poolCfg.testOnReturn = config.getBoolean("redis.$name.pool.testOnReturn")
        }

        config.existThenApply("redis.$name.pool.testWhileIdle") {
            poolCfg.testWhileIdle = config.getBoolean("redis.$name.pool.testWhileIdle")
        }

        config.existThenApply("redis.$name.pool.timeBetweenEvictionRunsMillis") {
            poolCfg.timeBetweenEvictionRunsMillis = config.getLong("redis.$name.pool.timeBetweenEvictionRunsMillis")
        }

        config.existThenApply("redis.$name.pool.numTestsPerEvictionRun") {
            poolCfg.numTestsPerEvictionRun = config.getInt("redis.$name.pool.numTestsPerEvictionRun")
        }

        config.existThenApply("redis.$name.pool.minEvictableIdleTimeMillis") {
            poolCfg.minEvictableIdleTimeMillis = config.getLong("redis.$name.pool.minEvictableIdleTimeMillis")
        }

        config.existThenApply("redis.$name.pool.softMinEvictableIdleTimeMillis") {
            poolCfg.softMinEvictableIdleTimeMillis = config.getLong("redis.$name.pool.softMinEvictableIdleTimeMillis")
        }

        config.existThenApply("redis.$name.pool.lifo") {
            poolCfg.lifo = config.getBoolean("redis.$name.pool.lifo")
        }

        config.existThenApply("redis.$name.pool.blockWhenExhausted") {
            poolCfg.blockWhenExhausted = config.getBoolean("redis.$name.pool.blockWhenExhausted")
        }

        return poolCfg
    }

    companion object {

        private val pools: Map<String, JRedisPool> = Application.config.getConfig("redis")
                .root()
                .map { Pair<String, JRedisPool>(it.key, JRedisPool(it.key)) }
                .toMap()

        fun default(): JRedisPool {
            return byName("default")
        }

        fun byName(name: String): JRedisPool {
            return pools[name]!!
        }
    }
}