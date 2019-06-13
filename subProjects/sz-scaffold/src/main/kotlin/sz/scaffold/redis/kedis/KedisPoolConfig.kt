package sz.scaffold.redis.kedis

import sz.scaffold.redis.kedis.pool.KedisAPI
import com.typesafe.config.Config
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getLongOrElse
import sz.scaffold.ext.getStringOrElse

//
// Created by kk on 2019-06-11.
//

class KedisPoolConfig : GenericObjectPoolConfig<KedisAPI>() {

    var operationTimeout: Long = -1L

    companion object {
        fun buildFrom(config: Config): KedisPoolConfig {
            val poolConfig = KedisPoolConfig().apply {
                // 基本参数

                // GenericObjectPool 提供了后进先出(LIFO)与先进先出(FIFO)两种行为模式的池。
                // 默认为true，即当池中有空闲可用的对象时，调用borrowObject方法会返回最近（后进）的实例
                lifo = config.getBooleanOrElse("lifo", true)

                // 当从池中获取资源或者将资源还回池中时 是否使用java.util.concurrent.locks.ReentrantLock.ReentrantLock 的公平锁机制,默认为false
                fairness = config.getBooleanOrElse("fairness", false)

                // 数量控制参数
                // 链接池中最大连接数,默认为8
                maxTotal = config.getIntOrElse("maxTotal", 8)
                // 链接池中最大空闲的连接数,默认也为8
                maxIdle = config.getIntOrElse("maxIdle", 8)
                // 连接池中最少空闲的连接数,默认为0
                minIdle = config.getIntOrElse("minIdle", 0)

                // 超时参数
                // 当连接池资源耗尽时，等待时间，超出则抛异常，默认为-1即永不超时
                maxWaitMillis = config.getLongOrElse("maxWaitMillis", 5000)
                // 当这个值为true的时候，maxWaitMillis参数才能生效。为false的时候，当连接池没资源，则立马抛异常。默认为true
                blockWhenExhausted = config.getBooleanOrElse("blockWhenExhausted", true)

                // test参数
                // 默认false，create的时候检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取
                testOnCreate = false // config.getBooleanOrElse("testOnCreate", false)
                // 默认false，borrow的时候检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取
                testOnBorrow = true // config.getBooleanOrElse("testOnBorrow", false)
                // 默认false，return的时候检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取
                testOnReturn = true // config.getBooleanOrElse("testOnReturn", false)
                // 默认false，在evictor线程里头，当evictionPolicy.evict方法返回false时，而且testWhileIdle为true的时候则检测是否有效，如果无效则移除
                testWhileIdle = config.getBooleanOrElse("testWhileIdle", true)

                // 驱逐检测参数
                // 空闲链接检测线程检测的周期，毫秒数。如果为负值，表示不运行检测线程。默认为-1
                timeBetweenEvictionRunsMillis = config.getLongOrElse("timeBetweenEvictionRunsMillis", 30000)
                // 在每次空闲连接回收器线程(如果有)运行时检查的连接数量，默认为3, 取-1时, 表示检查当前所有的idleObjects
                numTestsPerEvictionRun = config.getIntOrElse("numTestsPerEvictionRun", -1)
                // 连接空闲的最小时间，达到此值后空闲连接将可能会被移除
                minEvictableIdleTimeMillis = config.getLongOrElse("minEvictableIdleTimeMillis", 30000)
                // 连接空闲的最小时间，达到此值后空闲链接将会被移除，且保留minIdle个空闲连接数。默认为-1
                softMinEvictableIdleTimeMillis = config.getLongOrElse("softMinEvictableIdleTimeMillis", 1800000)
                // evict策略的类名，默认为org.apache.commons.pool2.impl.DefaultEvictionPolicy
                evictionPolicyClassName = config.getStringOrElse("evictionPolicyClassName", "org.apache.commons.pool2.impl.DefaultEvictionPolicy")

                // 额外参数
                operationTimeout = config.getLongOrElse("operationTimeout", -1)
            }


            return poolConfig
        }
    }
}




