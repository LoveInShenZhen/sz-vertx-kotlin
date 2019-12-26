package sz.scaffold.redis.kedis

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.redis.client.pingAwait
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import sz.scaffold.ext.chainToString
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019-06-10.
//

/**
 * Ref: https://vertx.io/docs/vertx-redis-client/java/#_pub_sub_mode
 * Redis supports queues and pub/sub mode, when operated in this mode once a connection invokes a subscriber mode
 * then it cannot be used for running other commands than the command to leave that mode.
 * So, we create a KedisPubSub object to manager it's own connection, include auto reconnect.
 */
class KedisPubSub(val vertx: Vertx, val options: RedisOptions, val reConnectInterval: Long = 5000, val pingInterval: Long = 10000) {

    private var onConnectHandler: ConnectHandler? = null
    private var onMessageHandler: MessageHandler? = null
    private var onExceptionHandler: ExceptionHandler? = null
    private var client: Redis? = null
    private var clientApi: RedisAPI? = null
    private var running = false

    val redisApi: RedisAPI?
        get() = this.clientApi

    fun onConnect(handler: ConnectHandler): KedisPubSub {
        this.onConnectHandler = handler
        return this
    }

    fun onMessage(handler: MessageHandler): KedisPubSub {
        this.onMessageHandler = handler
        return this
    }

    fun onException(handler: ExceptionHandler): KedisPubSub {
        this.onExceptionHandler = handler
        return this
    }

    fun start() {
        synchronized(this) {
            running = true
            GlobalScope.launch(vertx.dispatcher()) {
                connect()
                runPingTestLoop()
            }
        }
    }

    fun stop() {
        synchronized(this) {
            running = false
            stopPingTest()
        }
    }

    private suspend fun connect() {
        if (running.not()) {
            return
        }

        try {
            Logger.debug("开始连接 redis server")
            client = awaitResult {
                Redis.createClient(vertx, options).connect(it)
            }
            Logger.debug("Redis client 连接成功")
            clientApi = RedisAPI.api(client)
            client!!.exceptionHandler {
                Logger.debug("Redis client 发生异常,准备关闭client: $it")
                onExceptionHandler?.let { handler ->
                    try {
                        handler.invoke(it)
                    } catch (ex: Exception) {
                        Logger.error(ex.chainToString())
                    }
                }
                shutdownClient()
            }.endHandler {
                Logger.debug("Redis client 关闭: $it, ${reConnectInterval / 1000} 秒后重试连接")
                shutdownClient()
                GlobalScope.launch(vertx.dispatcher()) {
                    delay(reConnectInterval)
                    connect()
                }
            }.handler { response ->
                onMessageHandler?.let { handler ->
                    clientApi?.let { api ->
                        handler.invoke(api, response)
                    }
                }
            }

            // 调用 onConnectHandler 初始化client, 例如, 完成订阅的逻辑
            onConnectHandler?.let { handler ->
                clientApi?.let { api ->
                    handler.invoke(api)
                }
            }

            startPingTest()
        } catch (ex: Exception) {
            shutdownClient()
            Logger.debug("Redis client 连接失败: $ex, ${reConnectInterval / 1000} 秒后重试连接")
            GlobalScope.launch(vertx.dispatcher()) {
                delay(reConnectInterval)
                connect()
            }
        }
    }

    private fun shutdownClient() {
        synchronized(this) {
            client?.close()
            client = null
            clientApi = null
        }
    }

    private var needPingTest = false
    private fun stopPingTest() {
        needPingTest = false
        Logger.debug("Ping test OFF")
    }

    private fun startPingTest() {
        needPingTest = true
        Logger.debug("Ping test ON")
    }

    private fun runPingTestLoop() {
        GlobalScope.launch(vertx.dispatcher()) {
            while (running) {
                if (needPingTest) {
                    client?.let {
                        try {
                            val api = RedisAPI.api(it)
                            Logger.debug("开始 Ping 测试")
                            val response = withTimeout(1000) { api.pingAwait(emptyList()) }
                            Logger.debug("Ping test sucessed. $response")
                        } catch (ex: Exception) {
                            Logger.debug("Ping test failed. $ex")
                            Logger.debug("Ping test failed, ${reConnectInterval / 1000} 秒后重试连接")
                            stopPingTest()
                            shutdownClient()
                        }
                    }
                }
                delay(pingInterval)
            }
        }
    }


}

typealias ConnectHandler = (redisApi: RedisAPI) -> Unit
typealias MessageHandler = (redisApi: RedisAPI, response: Response) -> Unit
typealias ExceptionHandler = (ex: Throwable) -> Unit