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
object JRedisPool {

    private var _jedisPool: JedisPool? = null

    private val jedisPool: JedisPool
        get() {
            if (_jedisPool == null) {
                throw SzException("JedisPool 尚未完成初始化")
            }
            return _jedisPool!!
        }

    init {
//        public JedisPool(final GenericObjectPoolConfig poolConfig,
//                          final String host,
//                          int port,
//                          int timeout,
//                          final String password,
//                          final int database,
//                          final boolean ssl)
        _jedisPool = JedisPool(poolConfig(),
                Application.config.getString("redis.host"),
                Application.config.getInt("redis.port"),
                Application.config.getInt("redis.timeout"),
                Application.config.getStringOrNll("redis.password"),
                Application.config.getInt("redis.database"),
                Application.config.getBoolean("redis.ssl")
        )
        Logger.debug("初始化 JedisPool             [OK]")
    }

    fun jedis(): Jedis {
        return jedisPool.resource
    }

    private fun poolConfig(): JedisPoolConfig {
        val poolCfg = JedisPoolConfig()
        val config = Application.config
        config.existThenApply("redis.pool.maxIdle") {
            poolCfg.maxIdle = config.getInt("redis.pool.maxIdle")
        }

        config.existThenApply("redis.pool.minIdle") {
            poolCfg.minIdle = config.getInt("redis.pool.minIdle")
        }

        config.existThenApply("redis.pool.maxTotal") {
            poolCfg.maxTotal = config.getInt("redis.pool.maxTotal")
        }

        config.existThenApply("redis.pool.maxWaitMillis") {
            poolCfg.maxWaitMillis = config.getLong("redis.pool.maxWaitMillis")
        }

        config.existThenApply("redis.pool.testOnBorrow") {
            poolCfg.testOnBorrow = config.getBoolean("redis.pool.testOnBorrow")
        }

        config.existThenApply("redis.pool.testOnReturn") {
            poolCfg.testOnReturn = config.getBoolean("redis.pool.testOnReturn")
        }

        config.existThenApply("redis.pool.testWhileIdle") {
            poolCfg.testWhileIdle = config.getBoolean("redis.pool.testWhileIdle")
        }

        config.existThenApply("redis.pool.timeBetweenEvictionRunsMillis") {
            poolCfg.timeBetweenEvictionRunsMillis = config.getLong("redis.pool.timeBetweenEvictionRunsMillis")
        }

        config.existThenApply("redis.pool.numTestsPerEvictionRun") {
            poolCfg.numTestsPerEvictionRun = config.getInt("redis.pool.numTestsPerEvictionRun")
        }

        config.existThenApply("redis.pool.minEvictableIdleTimeMillis") {
            poolCfg.minEvictableIdleTimeMillis = config.getLong("redis.pool.minEvictableIdleTimeMillis")
        }

        config.existThenApply("redis.pool.softMinEvictableIdleTimeMillis") {
            poolCfg.softMinEvictableIdleTimeMillis = config.getLong("redis.pool.softMinEvictableIdleTimeMillis")
        }

        config.existThenApply("redis.pool.lifo") {
            poolCfg.lifo = config.getBoolean("redis.pool.lifo")
        }

        config.existThenApply("redis.pool.blockWhenExhausted") {
            poolCfg.blockWhenExhausted = config.getBoolean("redis.pool.blockWhenExhausted")
        }

        return poolCfg
    }
}