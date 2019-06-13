package sz.scaffold

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.impl.VertxImpl
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import jodd.exception.ExceptionUtil
import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.system.SystemInfo
import jodd.util.ClassLoaderUtil
import org.apache.commons.lang3.SystemUtils
import sz.scaffold.controller.ApiRoute
import sz.scaffold.controller.BodyHandlerOptions
import sz.scaffold.redis.kedis.pool.KedisPool
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService


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

    private val vertxImpl: VertxImpl
        get() {
            return vertx as VertxImpl
        }

    val workerPool: ExecutorService
        get() {
            return vertxImpl.workerPool
        }

    private var _vertoptions: VertxOptions? = null
    val vertxOptions: VertxOptions
        get() {
            if (_vertoptions == null) {
                throw SzException("Application 还没有初始化 vertx")
            }
            return _vertoptions!!
        }

    init {
        writePidFile()

        Logger.debug("current dir: ${File("").absolutePath}")

        val confFolder = File(FileNameUtil.concat(SystemUtils.getUserDir().absolutePath, "conf"))
        if (confFolder.exists()) {
            appHome = SystemUtils.getUserDir().absolutePath
            Logger.debug("appHome is UserDir: $appHome")
        } else {
            if (SystemUtils.getUserDir().name == "bin") {
                appHome = SystemUtils.getUserDir().parent
                Logger.debug("appHome find by bin folder path: $appHome")
            } else {
                val jarFile = ClassLoaderUtil.getDefaultClasspath().find { it.name.startsWith("kotlin-stdlib-") }
                    ?: throw SzException("class path 里不包含 kotlin-stdlib-*.jar, 请检查build.gradle")
                appHome = File(jarFile.parent).parent
                Logger.debug("appHome find by class lib folder path: $appHome")
            }
        }

        val confPath = FileNameUtil.concat(appHome, "conf/application.conf")
        if (File(confPath).exists()) {
            Logger.debug("""System.setProperty("config.file", "conf/application.conf") # application.conf full path = $confPath""")
            System.setProperty("config.file", confPath)
        }
        config = ConfigFactory.load()

        this.regOnStartHandler(Int.MIN_VALUE) {
            Logger.debug("Application start ...", AnsiColor.GREEN)
        }

        this.regOnStopHanlder(Int.MAX_VALUE) {
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

        logClusterNodeId()
    }

    fun createVertx(): Vertx {
        this._vertoptions = buildVertxOptions()

        if (this._vertoptions!!.eventBusOptions.isClustered) {
            // 集群方式
            val clusterManagerName = this.config.getString("app.vertx.clusterManager")

            Logger.debug("Vertx 集群方式, Cluster Manager: $clusterManagerName")
            val future = CompletableFuture<Vertx>()

            when (clusterManagerName) {
                "Ignite" -> this._vertoptions!!.clusterManager = IgniteClusterManager()
                "Zookeeper" -> this._vertoptions!!.clusterManager = ZookeeperClusterManager()
                else -> throw SzException("app.vertx.clusterManager 配置错误, 只支持: Ignite 和 Zookeeper")
            }

            Vertx.clusteredVertx(this._vertoptions) { event: AsyncResult<Vertx> ->
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
            Logger.debug("Vertx 非集群方式")
            return Vertx.vertx(this._vertoptions)
        }

    }

    fun setupOnStartAndOnStop() {
        // 执行 OnStart 方法
        startHandlers.toSortedMap().flatMap { it.value }.forEach { it() }

        // 注册 OnStop 方法
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                stopHandlers.toSortedMap().flatMap { it.value }.forEach { it() }
            }
        })
    }

    private fun loadRouteFromFiles(files: List<File>): List<ApiRoute> {
        return files.map { file -> ApiRoute.parseFromFile(file) }
            .flatMap { it }
    }

    // 检查在 route 文件 和 subRoutes 目录下的 *.route 里定义的route 的 method + path 没有重复的
    private fun checkApiRoutes(apiRoutes: List<ApiRoute>) {
        val routeMap = mutableMapOf<String, Int>()
        apiRoutes.forEach {
            val key = "${it.method.name}  ${it.path}"
            val count = routeMap.getOrDefault(key, 0)
            routeMap[key] = count + 1
        }

        val errRoutes = routeMap.filter { it.value > 1 }.map { it.key }
        if (errRoutes.isNotEmpty()) {
            throw SzException("以下路由发生重复定义,请开发人员检查:\n${errRoutes.joinToString("\n")}")
        }

    }

    fun getFile(relativePath: String): File {
        return File(FileNameUtil.concat(appHome, relativePath))
    }

    // 从 conf/route 文件, 以及 conf/sub_routes/*.route 子路由文件里加载路由配置
    fun loadApiRouteFromRouteFiles(): List<ApiRoute> {
        val routeFiles = mutableListOf(getFile("conf/route"))
        val subRoutesFolder = getFile("conf/sub_routes")
        if (subRoutesFolder.exists() && subRoutesFolder.isDirectory) {
            val files = subRoutesFolder.walk().filter { it.isFile && it.extension == "route" }
            routeFiles.addAll(files)
        }

        val apiRoutes = loadRouteFromFiles(routeFiles)
        checkApiRoutes(apiRoutes)

        return apiRoutes
    }

    fun runHttpServer() {

        getHttpServer().listen()
    }

    fun getHttpServer(): HttpServer {

        val httpServerOptions = this.httpServerOptions()
        val httpServer = vertx.createHttpServer(httpServerOptions)
        val bodyHandlerOptions = this.bodyHandlerOptions()

        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create()
            .setMergeFormAttributes(bodyHandlerOptions.mergeFormAttributes)
            .setBodyLimit(bodyHandlerOptions.bodyLimit)
            .setDeleteUploadedFilesOnEnd(bodyHandlerOptions.deleteUploadedFilesOnEnd)
            .setUploadsDirectory(bodyHandlerOptions.uploadsDirectory))

        router.route().handler(CookieHandler.create())

        loadApiRouteFromRouteFiles().forEach {
            it.addToRoute(router)
        }

        httpServer.requestHandler {
            try {
                // enable chunked responses because we will be adding data as
                // we execute over other handlers. This is only required once and
                // only if several handlers do output.
                it.response().isChunked = true

                if (it.method() == HttpMethod.POST) {
                    it.isExpectMultipart = true
                }

//                router.accept(it)
                router.handle(it)
            } catch (ex: Exception) {
                it.response().end("${ex.message}\n\n${ExceptionUtil.exceptionChainToString(ex)}")
            }

        }

        Logger.debug("Start http server at: ${httpServerOptions.host}:${httpServerOptions.port}")
        return httpServer
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
        val vertxOptFile = File(FileNameUtil.concat(this.appHome, "conf/vertxOptions.json"))
        if (FileUtil.isExistingFile(vertxOptFile)) {
            // conf/vertxOptions.json 配置文件存在, 则根据配置文件, 设置 VertxOptions
            val jsonOpts = JsonObject(FileUtil.readString(vertxOptFile))
            val opts = VertxOptions(jsonOpts)

            // 需要修正 clusterHost, 不然不同主机节点之间的 EventBus 不会互通
            val hostIp = InetAddress.getLocalHost().hostAddress
            // conf/vertxOptions.json 文件里 如果有配置 clusterHost, 则以配置文件的为有效
            // 否则, 以获取到的主机的IP地址为 clusterHost
            // 如果获取不到主机的IP地址, 则继续使用默认的 localhost
            if (jsonOpts.containsKey("clusterHost").not() && hostIp.isNullOrBlank().not()) {
                // 配置文件中不包含 clusterHost 配置项, 并且获取到的主机IP不为空
                opts.eventBusOptions.host = hostIp
            }
            return opts
        } else {
            val opts = VertxOptions()
            val hostIp = InetAddress.getLocalHost().hostAddress
            if (hostIp.isNullOrBlank().not()) {
                // 获取到的主机IP不为空
                opts.eventBusOptions.host = hostIp
            }
            return opts
        }
    }

    private fun httpServerOptions(): HttpServerOptions {
        val httpCfg = config.getConfig("app.httpServer") // 资源文件里的 reference.conf 包含默认配置,所以该configPath必然存在

        val cfgMap = httpCfg.root().map {
            if (it.key == "port") {
                Pair<String, Any>(it.key, it.value.unwrapped().toString().toInt())
            } else {
                Pair<String, Any>(it.key, it.value.unwrapped())
            }
        }.toMap()

        val httpServerOptions =  HttpServerOptions(JsonObject(cfgMap))
        if (SystemInfo().isLinux) {
            // Vert.x can run with native transports (when available) on BSD (OSX) and Linux:
            // 参考: https://vertx.io/docs/vertx-core/kotlin/#_native_transports
            httpServerOptions.isTcpFastOpen = config.getBoolean("app.httpServer.networkOptions.tcpFastOpen")
            httpServerOptions.isTcpCork = config.getBoolean("app.httpServer.networkOptions.tcpCork")
            httpServerOptions.isTcpQuickAck = config.getBoolean("app.httpServer.networkOptions.tcpQuickAck")
            httpServerOptions.isReusePort = config.getBoolean("app.httpServer.networkOptions.reusePort")
        }

        return httpServerOptions
    }

    private fun bodyHandlerOptions(): BodyHandlerOptions {
        return BodyHandlerOptions().apply {
            this.bodyLimit = config.getLong("app.httpServer.bodyHandler.bodyLimit")
            this.mergeFormAttributes = config.getBoolean("app.httpServer.bodyHandler.mergeFormAttributes")
            this.deleteUploadedFilesOnEnd = config.getBoolean("app.httpServer.bodyHandler.deleteUploadedFilesOnEnd")
            this.uploadsDirectory = FileNameUtil.concat(appHome, config.getString("app.httpServer.bodyHandler.uploadsDirectory"))
        }
    }

    private fun queryPid(): Long {
        return ManagementFactory.getRuntimeMXBean().name.split("@")[0].toLong()
    }

    private fun writePidFile() {
        val pidFilePath = System.getProperty("pidfile.path")
        if (!pidFilePath.isNullOrBlank()) {
            FileUtil.writeString(pidFilePath, queryPid().toString())
        }
    }

    private fun logClusterNodeId() {
        if (Application.vertxOptions.eventBusOptions.isClustered) {
            Logger.info("NodeId: ${Application.vertxOptions.clusterManager.nodeID}")
            Logger.info("Cluster Nodes: ${Application.vertxOptions.clusterManager.nodes.joinToString(", ")}")
        }
    }

    fun initRedisPool() {
        KedisPool.initPool()
    }
}