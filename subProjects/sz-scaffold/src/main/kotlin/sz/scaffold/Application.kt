package sz.scaffold

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import jodd.exception.ExceptionUtil
import jodd.util.SystemUtil
import sz.scaffold.controller.ApiRoute
import sz.scaffold.tools.logger.Logger
import java.io.File

//
// Created by kk on 17/8/19.
//
object Application {

    private var _onStartHanler: () -> Unit = {
        Logger.debug("Application start ...")
    }

    private var _onStopHandler: () -> Unit = {
        Logger.debug("Application stop ...")
    }

    val config: Config
    val appHome: String
    val classLoader = Application::class.java.classLoader

    init {
        System.setProperty("config.file", "conf/application.conf")
        config = ConfigFactory.load()
        appHome = SystemUtil.workingFolder()
    }

    fun run(appVertx: Vertx? = null) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                _onStopHandler()
            }
        })

        val vertx = appVertx ?: Vertx.vertx(this.vertxOptions())
        val httpServerOptions = this.httpServerOptions()
        val httpServer = vertx.createHttpServer(httpServerOptions)
        _onStartHanler()

//        Logger.debug(httpServerOptions.toJson().encodePrettily())

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

        Logger.debug("Start http server at: ${httpServerOptions.host}:${httpServerOptions.port}")
        httpServer.listen()
    }

    fun onStart(block: () -> Unit) {
        _onStartHanler = block
    }

    fun onStop(block: () -> Unit) {
        _onStopHandler = block
    }

    private fun vertxOptions(): VertxOptions {
        val opts = VertxOptions()
        // TODO: setup VertxOptions by configuration
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
}