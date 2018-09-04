package sz.scaffold

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import jodd.exception.ExceptionUtil
import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.util.ClassLoaderUtil
import jodd.util.SystemUtil
import sz.scaffold.controller.ApiRoute
import sz.scaffold.controller.BodyHandlerOptions
import sz.scaffold.tools.SzException
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.lang.management.ManagementFactory
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

        System.setProperty("config.file", "conf/application.conf")
        config = ConfigFactory.load()

        val confFolder = File(FileNameUtil.concat(SystemUtil.workingFolder(), "conf"))
        if (confFolder.exists()) {
            appHome = SystemUtil.workingFolder()
        } else {
            // 从 class path 里查找 conf 结尾的路径
            val confFile = ClassLoaderUtil.getDefaultClasspath().find { it.name == "conf" }
                    ?: throw SzException("class path 里不包含 conf 目录, 请检查启动环境脚本")
            appHome = confFile.parent
        }

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

        if (this._vertoptions!!.isClustered) {
            // 集群方式
            val clusterManagerName = this.config.getString("app.vertx.clusterManager")

            Logger.debug("Vertx 集群方式, Cluster Manager: $clusterManagerName")
            val future = CompletableFuture<Vertx>()

            when(clusterManagerName) {
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

    // 从 conf/route 文件, 以及 conf/sub_routes/*.route 子路由文件里加载路由配置
    fun loadApiRouteFromRouteFiles(): List<ApiRoute> {
        val routeFiles = mutableListOf(File("conf/route"))
        val subRoutesFolder = File("conf/sub_routes")
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

                router.accept(it)
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
        val opts = if (FileUtil.isExistingFile(vertxOptFile)) {
            val jsonOpts = JsonObject(FileUtil.readString(vertxOptFile))
            VertxOptions(jsonOpts)
        } else {
            VertxOptions()
        }

        return opts
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

        return HttpServerOptions(JsonObject(cfgMap))

    }

    private fun bodyHandlerOptions(): BodyHandlerOptions {
        val cfgPath = "app.httpServer.bodyHandler"
        if (config.hasPath(cfgPath)) {
            val httpCfg = config.getConfig(cfgPath)

            val cfgMap = httpCfg.root().map {
                if (it.key == "bodyLimit") {
                    Pair<String, Any>(it.key, it.value.unwrapped().toString().toLong())
                } else {
                    Pair<String, Any>(it.key, it.value.unwrapped())
                }
            }.toMap()

            return BodyHandlerOptions(JsonObject(cfgMap))
        } else {
            return BodyHandlerOptions()
        }


    }

    fun getFile(relativePath: String): File {
        val path = FileNameUtil.concat(appHome, relativePath)
        return File(path)
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
        if (Application.vertxOptions.isClustered) {
            Logger.info("NodeId: ${Application.vertxOptions.clusterManager.nodeID}")
            Logger.info("Cluster Nodes: ${Application.vertxOptions.clusterManager.nodes.joinToString(", ")}")
        }
    }
}