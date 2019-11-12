package sz.scaffold.redis.kedis

import com.typesafe.config.Config
import sz.objectPool.PoolConfig
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getLongOrElse

//
// Created by kk on 2019-06-11.
//

class KedisPoolConfig {

    var operationTimeout: Long = -1L

    lateinit var objectPoolConfig: PoolConfig

    companion object {
        fun buildFrom(config: Config): KedisPoolConfig {

            return KedisPoolConfig().apply {

                objectPoolConfig = PoolConfig(
                    // 数量控制参数
                    // 链接池中最大连接数,默认为8
                    maxTotal = config.getIntOrElse("maxTotal", 8),
                    // 链接池中最大空闲的连接数,默认也为8
                    maxIdle = config.getIntOrElse("maxIdle", 8),
                    // 连接池中最少空闲的连接数,默认为0
                    minIdle = config.getIntOrElse("minIdle", 0),
                    // 超时参数
                    // 当连接池资源耗尽时，等待时间，超出则抛异常，默认为-1即永不超时
                    borrowTimeoutMs = config.getLongOrElse("borrowTimeoutMs", 5000),
                    // 驱逐检测的间隔时间, 默认10分钟
                    timeBetweenEvictionRunsSeconds = config.getIntOrElse("timeBetweenEvictionRunsSeconds", 600)
                )

                // 额外参数
                operationTimeout = config.getLongOrElse("operationTimeout", -1)
            }
        }
    }
}




