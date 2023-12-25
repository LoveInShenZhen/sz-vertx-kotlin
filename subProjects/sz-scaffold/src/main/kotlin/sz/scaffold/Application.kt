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
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import jodd.exception.ExceptionUtil
import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.system.SystemInfo
import jodd.util.ClassLoaderUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.apache.commons.lang3.SystemUtils
import sz.scaffold.controller.ApiRoute
import sz.scaffold.controller.BodyHandlerOptions
import sz.scaffold.dispatchers.IDispatcherFactory
import sz.scaffold.ext.changeWorkingDir
import sz.scaffold.ext.filePathJoin
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger
import sz.scaffold.websocket.WebSocketFilter
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import kotlinx.coroutines.runBlocking as kxRunBlocking


//
// Created by kk on 17/8/19.
//
@Suppress("MemberVisibilityCanBePrivate", "HasPlatformType", "ObjectPropertyName")
object Application {

    private val startHandlers = mutableMapOf<Int, MutableList<() -> Unit>>()
    private val stopHandlers = mutableMapOf<Int, MutableList<() -> Unit>>()

    val config: Config
    val appHome: String
    val classLoader = Application::class.java.classLoader
    val inProductionMode: Boolean

    private var _vertx: Vertx? = null
    private const val szPropertiesUrlKey = "sz.properties.url"

    val vertx: Vertx
        get() {
            if (_vertx == null) {
                throw SzException("Application has not initialized vertx.")
            }
            return _vertx!!
        }

    private val vertxImpl: VertxImpl
        get() {
            return vertx as VertxImpl
        }

    val workerPool: ExecutorService
        get() {
            return vertxImpl.workerPool.executor()
        }

    private var _vertoptions: VertxOptions? = null
    val vertxOptions: VertxOptions
        get() {
            if (_vertoptions == null) {
                throw SzException("Application has not initialized vertx.")
            }
            return _vertoptions!!
        }

    init {
        writePidFile()

        // setup use SLF4JLogDelegateFactory
        // ref: https://vertx.io/docs/vertx-core/kotlin/#_using_another_logging_framework
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

        loadProperties()

        val confFolder = File(filePathJoin(SystemUtils.getUserDir().absolutePath, "conf"))
        if (confFolder.exists()) {
            appHome = SystemUtils.getUserDir().absolutePath
        } else {
            if (SystemUtils.getUserDir().name == "bin" &&
                    SystemUtils.getUserDir().parentFile.hasFile("conf${File.separator}application.conf")
            ) {
                appHome = SystemUtils.getUserDir().parent
            } else {
                val jarFile = ClassLoaderUtil.getDefaultClasspath().find { it.name.startsWith("kotlin-stdlib-") }
                        ?: throw SzException("class path 里不包含 kotlin-stdlib-*.jar, 请检查build.gradle")
                appHome = File(jarFile.parent).parent
            }
        }

        val logbackXmlPath = filePathJoin(appHome, "conf", "logback.xml")
        setupConfPathProperty("logback.configurationFile", logbackXmlPath)

        val currentDir = File("").absolutePath
        Logger.debug("current dir: $currentDir")
        Logger.debug("appHome : $appHome")
        Logger.debug("""-Dlogback.configurationFile : ${System.getProperty("logback.configurationFile")}""")

        Logger.debug("Change working dir to appHome")
        changeWorkingDir(appHome)

        val confPath = filePathJoin(appHome, "conf", "application.conf")
        if (File(confPath).exists().not()) {
            throw SzException("appHome 路径推断错误, 因为 [$confPath] 不存在")
        }

        if (System.getProperties().containsKey("config.url")) {
            Logger.debug("""-Dconfig.url : ${System.getProperty("config.url")}""")
        } else {
            setupConfPathProperty("config.file", confPath)
            Logger.debug("""-Dconfig.file : ${System.getProperty("config.file")}""")
        }

        config = ConfigFactory.load()

        inProductionMode = config.getBoolean("app.httpServer.productionMode")

        this.regOnStartHandler(Int.MIN_VALUE) {
            Logger.info("Application start ...")
        }

        this.regOnStopHanlder(Int.MAX_VALUE) {
            Logger.info("Application stop ...")
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
                Logger.info("Stop vertx successfully.")
            } else {
                Logger.error("Stop vertx failed.")
            }
        }
    }

    private fun loadProperties() {
        try {
            val propertiesUrl = System.getProperty(szPropertiesUrlKey, "")

            if (propertiesUrl.isNotBlank()) {
                val url = URI.create(propertiesUrl).toURL()  //URL(propertiesUr)
                url.openStream().use {
                    val properties = Properties()
                    properties.load(it)
                    properties.forEach { prop ->
                        System.setProperty(prop.key.toString(), prop.value.toString())
                    }
                }
            }
        } catch (ex: Exception) {
            throw RuntimeException("Failed to load global properties. Please check whether the config -D$szPropertiesUrlKey is valid.")
        }
    }

    private fun setupConfPathProperty(propName: String, default: String) {
        if (System.getProperties().containsKey(propName).not()) {
            // If it is not specified, it will be specified as our default value
            System.setProperty(propName, default)
        }
    }

    fun setupVertx(appVertx: Vertx? = null) {
        if (_vertx != null) {
            throw SzException("The vertx of Application has been initialized. Do not initialize it again.")
        }
        _vertx = appVertx ?: createVertx()

        logClusterNodeId()
    }

    fun createVertx(): Vertx {
        this._vertoptions = buildVertxOptions()

        if (this.isClustered) {
            Logger.info("Vertx: cluster mode")
            val future = CompletableFuture<Vertx>()

            val zookeeperConfig = JsonObject(config.getConfig("app.vertx.zookeeper").root().unwrapped().toShortJson())
            this._vertoptions!!.clusterManager = ZookeeperClusterManager(zookeeperConfig)

            Vertx.clusteredVertx(this._vertoptions) { event: AsyncResult<Vertx> ->
                if (event.failed()) {
                    throw SzException("Failed to create cluster mode Vertx: ${event.cause().message}")
                } else {
                    future.complete(event.result())
                }
            }
            return future.get()
        } else {
            Logger.info("Vertx: standalone mode")
            return Vertx.vertx(this._vertoptions)
        }

    }

    val isClustered: Boolean
        get() {
            return config.getBoolean("app.vertx.options.clustered")
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
            if (it.path.startsWith("/builtinstatic/")) {
                throw SzException("[/builtinstatic/*] is designed to deal with static file resources, please change the routing path definition: ${it.path}")
            }
            val key = "${it.method.name()}  ${it.path}"
            val count = routeMap.getOrDefault(key, 0)
            routeMap[key] = count + 1
        }

        val errRoutes = routeMap.filter { it.value > 1 }.map { it.key }
        if (errRoutes.isNotEmpty()) {
            throw SzException(
                    "The following routes are repeatedly defined, please check route file:\n${
                        errRoutes.joinToString(
                                "\n"
                        )
                    }"
            )
        }

    }

    fun getFile(relativePath: String): File {
        return File(filePathJoin(appHome, relativePath))
    }

    // 从 conf/route 文件, 以及 conf/sub_routes/*.route 子路由文件里加载路由配置
    fun loadApiRouteFromRouteFiles(): List<ApiRoute> {

        val routeFiles = mutableListOf(getFile("conf${File.separator}route"))    // conf/route
        val subRoutesFolder = getFile("conf${File.separator}sub_routes")                    // conf/sub_routes
        if (subRoutesFolder.exists() && subRoutesFolder.isDirectory) {
            val files = subRoutesFolder.walk().filter { it.isFile && it.extension == "route" }
            routeFiles.addAll(files)
        }

        val apiRoutes = loadRouteFromFiles(routeFiles)
        checkApiRoutes(apiRoutes)

        return apiRoutes
    }

    // 从 conf/route.websocket 文件, 加载 webSocket 配置
    fun setupWebSocketHandler(httpServer: HttpServer) {
        val routeFile = getFile("conf${File.separator}route.websocket")             // conf/route.websocket
        if (routeFile.exists()) {
            val routeRegex = """(/\S*)\s+(\S+)\s*$""".toRegex()
            val lines = routeFile.readLines().map { it.trim() }
                    .filter { it.startsWith("#").not() && it.startsWith("//").not() && it.isNotBlank() }
            if (lines.isNotEmpty()) {
                val webSocketRootHandler = WebSocketFilter(vertx)
                lines.forEach { line ->
                    if (routeRegex.matches(line)) {
                        val parts = routeRegex.matchEntire(line)!!.groupValues
                        val path = parts[1].trim()
                        val handlerClassName = parts[2].trim()
                        webSocketRootHandler.addPathAndHandler(path, handlerClassName)
                    } else {
                        throw SzException("websocket route definition syntax error: $line")
                    }
                }

                httpServer.webSocketHandler(webSocketRootHandler)
            }
        }
    }

    fun runHttpServer() {
        createHttpServer().listen()
    }

    fun createHttpServer(): HttpServer {

        val httpServerOptions = this.httpServerOptions()
        val httpServer = vertx.createHttpServer(httpServerOptions)
        val bodyHandlerOptions = this.bodyHandlerOptions()

        val router = Router.router(vertx)

        // /builtinstatic/* , 该 path 是约定专门用于处理静态文件的
        router.route("/builtinstatic/*").handler(StaticHandler.create())

        router.route().handler(
                BodyHandler.create()
                        .setMergeFormAttributes(bodyHandlerOptions.mergeFormAttributes)
                        .setBodyLimit(bodyHandlerOptions.bodyLimit)
                        .setDeleteUploadedFilesOnEnd(bodyHandlerOptions.deleteUploadedFilesOnEnd)
                        .setUploadsDirectory(bodyHandlerOptions.uploadsDirectory)
        )

        loadApiRouteFromRouteFiles().forEach {
            it.addToRoute(router)
        }

        httpServer.requestHandler {
            try {
                // enable chunked responses because we will be adding data as
                // we execute over other handlers. This is only required once and
                // only if several handlers do output.

                // 排除 /builtinstatic/* , 该 path 是约定专门用于处理静态文件的
                if (it.path().startsWith("/builtinstatic/").not()) {
                    it.response().isChunked = true
                    if (it.method() == HttpMethod.POST) {
                        it.isExpectMultipart = true
                    }
                }

                router.handle(it)
            } catch (ex: Exception) {
                it.response().end("${ex.message}\n\n${ExceptionUtil.exceptionChainToString(ex)}")
            }
        }

        setupWebSocketHandler(httpServer)

        Logger.info("Start http server at: http://localhost:${httpServerOptions.port}")
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
        try {
            val configContent = config.getConfig("app.vertx.options").root().unwrapped().toShortJson()

            val jsonOpts = JsonObject(configContent)
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
        } catch (ex: Exception) {
            throw RuntimeException("Failed to create vertx options: ${ex.message}")
        }
    }

    private fun httpServerOptions(): HttpServerOptions {
        val httpCfg = config.getConfig("app.httpServer.httpOptions") // 资源文件里的 reference.conf 包含默认配置,所以该configPath必然存在

        val cfgMap = httpCfg.root().map { Pair<String, Any>(it.key, it.value.unwrapped()) }.toMap().toMutableMap()
        val httpOptionJson = JsonObject(cfgMap)
        httpOptionJson.put("host", config.getString("app.httpServer.host"))
        httpOptionJson.put("port", config.getInt("app.httpServer.port"))

        val httpServerOptions = HttpServerOptions(httpOptionJson)
        if (SystemInfo().isLinux) {
            // Vert.x can run with native transports (when available) on BSD (OSX) and Linux:
            // 参考: https://vertx.io/docs/vertx-core/kotlin/#_native_transports
            httpServerOptions.isTcpFastOpen = config.getBoolean("app.httpServer.networkOptions.tcpFastOpen")
            httpServerOptions.isTcpCork = config.getBoolean("app.httpServer.networkOptions.tcpCork")
            httpServerOptions.isTcpQuickAck = config.getBoolean("app.httpServer.networkOptions.tcpQuickAck")
            httpServerOptions.isReusePort = config.getBoolean("app.httpServer.networkOptions.reusePort")
        }
        if (SystemInfo().isMacOsX) {
            httpServerOptions.isReusePort = config.getBoolean("app.httpServer.networkOptions.reusePort")
        }

        httpServerOptions.maxWebSocketFrameSize = config.getInt("app.httpServer.webSocket.maxWebSocketFrameSize")
        httpServerOptions.maxWebSocketMessageSize = config.getInt("app.httpServer.webSocket.maxWebSocketMessageSize")

        return httpServerOptions
    }

    private fun bodyHandlerOptions(): BodyHandlerOptions {
        return BodyHandlerOptions().apply {
            this.bodyLimit = config.getLong("app.httpServer.bodyHandler.bodyLimit")
            this.mergeFormAttributes = config.getBoolean("app.httpServer.bodyHandler.mergeFormAttributes")
            this.deleteUploadedFilesOnEnd = config.getBoolean("app.httpServer.bodyHandler.deleteUploadedFilesOnEnd")
            this.uploadsDirectory =
                    filePathJoin(appHome, config.getString("app.httpServer.bodyHandler.uploadsDirectory"))
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
        if (this.isClustered) {
            Logger.info("NodeId: ${vertxOptions.clusterManager.nodeId}")
            Logger.info("Cluster Nodes: ${vertxOptions.clusterManager.nodes.joinToString(", ")}")
        }
    }

    private fun File.hasFile(path: String): Boolean {
        val fullPath = FileNameUtil.concat(this.path, path)
        return File(fullPath).exists()
    }

    val workerDispatcher: CoroutineDispatcher by lazy {
        val factory = Class.forName(config.getString("app.httpServer.dispatcher.factory")).getDeclaredConstructor()
                .newInstance() as IDispatcherFactory
        factory.build()
    }

    /// CoroutineScope for http server
    val workerScope: CoroutineScope by lazy {
        CoroutineScope(workerDispatcher)
    }

    fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
        return kxRunBlocking(context = workerDispatcher, block = block)
    }
}