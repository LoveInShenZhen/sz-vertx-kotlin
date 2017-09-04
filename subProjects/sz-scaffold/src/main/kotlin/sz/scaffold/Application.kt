package sz.scaffold

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import jodd.exception.ExceptionUtil
import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.util.SystemUtil
import sz.scaffold.controller.ApiRoute
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.util.concurrent.CompletableFuture

//
// Created by kk on 17/8/19.
//
object Application {

    private val startHandlers = mutableMapOf<Int, MutableList<() -> Unit>>()
    private val stopHandlers = mutableMapOf<Int, MutableList<() -> Unit>>()

    val config: Config
    val appHome: String
    val classLoader = Application::class.java.classLoader

    private var _vertx: Vertx? = null

    val vertx: Vertx
        get() {
            if (_vertx == null) {
                throw SzException("Application 还没有初始化 vertx")
            }
            return _vertx!!
        }

    init {
        System.setProperty("config.file", "conf/application.conf")
        config = ConfigFactory.load()
        appHome = SystemUtil.workingFolder()

        this.regOnStartHandler(Int.MIN_VALUE) {
            Logger.debug("Application start ...", AnsiColor.GREEN)
        }

        this.regOnStopHanlder(Int.MIN_VALUE) {
            Logger.debug("Application stop ...", AnsiColor.GREEN)
            val stopVertxFuture = CompletableFuture<Boolean>()
            vertx.close { res ->
                if (res.failed()) {
                    Logger.error(ExceptionUtil.exceptionChainToString(res.cause()))
                    stopVertxFuture.complete(false)
                } else {
                    stopVertxFuture.complete(true)
                }
            }
            if (stopVertxFuture.get()) {
                Logger.info("Stop vertx successed.")
            } else {
                Logger.error("Stop vertx failed.")
            }
        }
    }

    fun setupVertx(appVertx: Vertx? = null) {
        if (_vertx != null) {
            throw SzException("Application 的 vertx 已经初始化过了, 请勿重复初始化")
        }
        _vertx = appVertx ?: createVertx()
    }

    fun createVertx() : Vertx {
        val clustered = config.getBooleanOrElse("app.vertx.clustered", false)
        if (clustered) {
            // 集群方式
            val future = CompletableFuture<Vertx>()
            Vertx.clusteredVertx(buildVertxOptions().setClustered(true)) {
                event: AsyncResult<Vertx> ->
                if (event.failed()) {
                    Logger.error("创建集群方式Vertx失败:\n${ExceptionUtil.exceptionChainToString(event.cause())}")
                    throw SzException("创建集群方式Vertx失败: ${event.cause().message}")
                } else {
                    future.complete(event.result())
                }
            }
            return future.get()
        } else {
            // 非集群方式
            return Vertx.vertx(this.buildVertxOptions())
        }

    }

    fun runHttpServer() {

        val httpServerOptions = this.httpServerOptions()
        val httpServer = vertx.createHttpServer(httpServerOptions)

        startHandlers.toSortedMap().flatMap { it.value }.forEach { it() }

        val router = Router.router(vertx)

        val routeFile = File("conf/route")
        ApiRoute.parseFromFile(routeFile).forEach {
            it.addToRoute(router)
        }

        httpServer.requestHandler {
            try {
                // enable chunked responses because we will be adding data as
                // we execute over other handlers. This is only required once and
                // only if several handlers do output.
                it.response().isChunked = true
                router.accept(it)
            } catch (ex: Exception) {
                it.response().end("${ex.message}\n\n${ExceptionUtil.exceptionChainToString(ex)}")
            }

        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                stopHandlers.toSortedMap().flatMap { it.value }.forEach { it() }
            }
        })

        Logger.debug("Start http server at: ${httpServerOptions.host}:${httpServerOptions.port}")
        httpServer.listen()
    }

    fun regOnStartHandler(priority: Int = 100, block: () -> Unit): Application {
        val handlerList = if (startHandlers.containsKey(priority)) {
            startHandlers[priority]!!
        } else {
            startHandlers.put(priority, mutableListOf())
            startHandlers[priority]!!
        }

        handlerList.add(block)
        return this
    }

    fun regOnStopHanlder(priority: Int = 100, block: () -> Unit): Application {
        val handlerList = if (stopHandlers.containsKey(priority)) {
            stopHandlers[priority]!!
        } else {
            stopHandlers.put(priority, mutableListOf())
            stopHandlers[priority]!!
        }

        handlerList.add(block)
        return this
    }

    private fun buildVertxOptions(): VertxOptions {
        val vertxOptFile = File("conf/vertxOptions.json")
        val opts = if (FileUtil.isExistingFile(vertxOptFile)) {
            val jsonOpts = JsonObject(FileUtil.readString(vertxOptFile))
            VertxOptions(jsonOpts)
        } else {
            VertxOptions()
        }
        
        return opts
    }

    private fun httpServerOptions(): HttpServerOptions {
        val httpCfg = config.getConfig("app.httpServer")

        val cfgMap = httpCfg.root().map {
            if (it.key == "port") {
                Pair<String, Any>(it.key, it.value.unwrapped().toString().toInt())
            } else {
                Pair<String, Any>(it.key, it.value.unwrapped())
            }
        }.toMap()

        return HttpServerOptions(JsonObject(cfgMap))

    }

    fun getFile(relativePath: String) : File {
        val path = FileNameUtil.concat(appHome, relativePath)
        return File(path)
    }
}