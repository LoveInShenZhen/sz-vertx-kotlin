package sz.scaffold.controller

import com.fasterxml.jackson.databind.JsonNode
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import jodd.exception.ExceptionUtil
import jodd.util.ClassUtil
import kotlinx.coroutines.async
import sz.logger.log
import sz.scaffold.Application
import sz.scaffold.annotations.PostForm
import sz.scaffold.annotations.PostJson
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.aop.interceptors.GlobalInterceptorBase
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.Json.toStrMap
import sz.scaffold.tools.json.singleLineJson
import java.io.File
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

//
// Created by kk on 17/8/16.
//
@Suppress("DuplicatedCode")
data class ApiRoute(val method: HttpMethod,
                    val path: String,
                    val controllerKClass: KClass<*>,
                    val controllerFun: KFunction<*>,
                    val defaults: Map<String, String>) {

    private val interceptorList: List<Interceptor> = Interceptor.buildOf(controllerFun)

    fun addToRoute(router: Router) {
//        router.route(method, path).blockingHandler({ routingContext ->
//            callApi(routingContext)
//        }, false)
        // callApi(routingContext) will run on event loop
        router.route(method, path).handler { routingContext -> callApi(routingContext) }
    }

    private fun callApi(httpContext: RoutingContext) {
        val response = httpContext.response()
        val controller = controllerKClass.createInstance()
        val apiController = controller as ApiController
        apiController.setupContext(httpContext)

        val paramDatas = httpContext.queryParams(defaults)
        // 在此, 需要先检查控制器方法需要的参数, 在 query parameters 和 default 参数列表 里是否都存在

        val missingParameters = this.controllerFun.parameters
            .filter { !it.isOptional && it.kind == KParameter.Kind.VALUE }      // 排除掉控制器方法的可选参数和 实例() 参数
            .filter { !paramDatas.containsKey(it.name) && !this.defaults.containsKey(it.name) }

        if (missingParameters.isNotEmpty()) {
            // 说明缺少参数
            missingParameters.forEach {
                response.write("missing query parameter: ${it.name}\n")
            }
            response.putHeader("Content-Type", ContentTypes.Text)
            response.end()
            return
        }

        Application.workerScope.async {
            try {
                val args = controllerFun.buildCallArgs(apiController, paramDatas)
                val wrapperAction = buildWrappedAction(httpContext, args)
                // 通过控制器方法的返回类型, 是否是ReplyBase或者其子类型, 来判断是否是 json api 方法
                if (controllerFun.returnType.isSubtypeOf(ReplyBase::class.createType())) {
                    // 对于api请求, 要求浏览器端不缓存
                    response.putHeader("Cache-Control", "no-cache")
                    val actionResult = wrapperAction.call()
                    if (actionResult != null) {
                        if (isJsonpRequest(httpContext, actionResult)) {
                            onJsonp(httpContext, actionResult)
                        } else if (actionResult is ReplyBase) {
                            response.putHeader("Content-Type", ContentTypes.Json)
                            response.write(actionResult.singleLineJson())
                        }
                    }
                } else {
                    // 其他普通的 http 请求(非 api 请求)
                    val result = wrapperAction.call()
                    onNormal(httpContext, result)
                }
            } catch (ex: Throwable) {
                log.debug(ExceptionUtil.exceptionChainToString(ex))
                val reply = ReplyBase()
                val reason = ExceptionUtil.findCause(ex, BizLogicException::class.java)
                if (reason != null) {
                    reply.onError(reason)
                } else {
                    reply.onError(ex)
                }
                if (httpContext.queryParams(mapOf()).containsKey("callback")) {
                    response.putHeader("Content-Type", ContentTypes.JavaScript)
                    val callback = httpContext.queryParams(mapOf()).getValue("callback")
                    val body = "$callback(${reply.singleLineJson()});"
                    response.write(body)
                } else {
                    response.putHeader("Content-Type", ContentTypes.Json)
                    response.write(reply.singleLineJson())
                }
            }

            if (!response.ended()) {
                response.end()
            }
        }.invokeOnCompletion { ex ->
            if (ex != null) {
                val reply = ReplyBase()
                val reason = ExceptionUtil.findCause(ex, BizLogicException::class.java)
                if (reason != null) {
                    reply.onError(reason)
                } else {
                    reply.onError(ex)
                }
                if (httpContext.queryParams(mapOf()).containsKey("callback")) {
                    response.putHeader("Content-Type", ContentTypes.JavaScript)
                    val callback = httpContext.queryParams(mapOf()).getValue("callback")
                    val body = "$callback(${reply.singleLineJson()});"
                    response.write(body)
                } else {
                    response.putHeader("Content-Type", ContentTypes.Json)
                    response.write(reply.singleLineJson())
                }
            }
            if (!response.ended()) {
                response.end()
            }
        }
    }

    private fun isJsonpRequest(httpContext: RoutingContext, result: Any?): Boolean {

        if (result == null || result == Unit) {
            return false
        }

        if (result is ReplyBase || result is JsonNode) {
            return httpContext.queryParams(mapOf()).containsKey("callback")
        }

        return false
    }

    private fun onJsonp(httpContext: RoutingContext, result: Any) {
        val response = httpContext.response()
        response.putHeader("Content-Type", ContentTypes.JavaScript)
        val callback = httpContext.queryParams(mapOf()).getValue("callback")
        val body = "$callback(${Json.toJsonStrPretty(result)});"
        response.write(body)
    }

    private fun onNormal(httpContext: RoutingContext, result: Any?) {
        val response = httpContext.response()

        if (response.ended()) {
            // response 已经在前面被结束, 则直接返回
            return
        }

        if (result == null || result == Unit) {
            return
        }

        if (result is ReplyBase || result is JsonNode) {
            response.write(result.singleLineJson())
            response.putHeader("Content-Type", ContentTypes.Json)
        } else if (result is String) {
            response.write(result.toString())
        }
        // 其他类型(非 ReplyBase, 非 JsonNode, 非 String), 不做处理
    }

    fun isJsonApi(): Boolean {
        return controllerFun.returnType.isSubtypeOf(ReplyBase::class.createType())
    }

    fun returnType(): KType {
        return controllerFun.returnType
    }

    fun postBodyClass(): KClass<*>? {
        if (method == HttpMethod.POST) {
            val annPostJson = controllerFun.findAnnotation<PostJson>()
            if (annPostJson != null) return annPostJson.value

            val annPostForm = controllerFun.findAnnotation<PostForm>()
            if (annPostForm != null) return annPostForm.value

            return null

        } else {
            return null
        }
    }

    private fun wrapperSuspendFunction(controllerFun: KFunction<*>, httpContext: RoutingContext, args: Map<KParameter, Any?>): Action<*> {
        val action = object : Action<Any>() {
            override suspend fun call(): Any? {
                return controllerFun.callSuspendBy(args)
            }
        }

        action.setupHttpContext(httpContext)
        return action
    }

    private fun buildWrappedAction(httpContext: RoutingContext, args: Map<KParameter, Any?>): Action<*> {
        var delegateAction = wrapperSuspendFunction(controllerFun, httpContext, args)

        this.interceptorList.forEach {
            delegateAction = it.chainedWith(httpContext, delegateAction)
        }

        return delegateAction
    }

    override fun toString(): String {
        return "${this.method} ${this.path} ${this.controllerKClass.qualifiedName} ${this.controllerFun.name}() ${this.defaults}"
    }

    companion object {

        private val builtinRouteControllers: Set<String> by lazy {
            setOf(
                "sz.scaffold.controller.builtIn.Default.sysInfo",
                "controllers.builtin.szebean.CreateTablesSql",
                "controllers.builtin.szebean.DropTablesSql",
                "controllers.builtin.szebean.CreateIndexSql",
                "controllers.builtin.szebean.evolution",
                "sz.api.controllers.ApiDoc.apiIndex",
                "sz.api.controllers.ApiDoc.apiTest",
                "sz.api.controllers.ApiDoc.apiDocMarkdown",
                "sz.api.controllers.ApiDoc.apiDocHtml",
                "sz.api.controllers.ApiDoc.pageIndex",
                "sz.api.controllers.ApiDoc.pageTest"
            )
        }

        private fun KParameter.ParserVal(data: Map<String, String>): Any? {
            if (this.kind != KParameter.Kind.VALUE) {
                throw SzException("参数种类为: ${this.kind}")
            }
            val value = data[this.name!!]
            if (value == null) {
                if (this.type.isMarkedNullable) {
                    return null
                } else {
                    throw SzException("缺少参数: ${this.name} : ${this.type}")
                }
            }

            // 根据参数类型, 从字符串转换成对应的类型, 并且只支持下列类型
            try {
                return when (type.toString().replace("?", "")) {
                    "kotlin.String" -> value
                    "kotlin.Int" -> value.toInt()
                    "kotlin.Short" -> value.toShort()
                    "kotlin.Byte" -> value.toByte()
                    "kotlin.Long" -> value.toLong()
                    "kotlin.Float" -> value.toFloat()
                    "kotlin.Double" -> value.toDouble()
                    "kotlin.Boolean" -> value.toBoolean()
                    "java.math.BigDecimal" -> BigDecimal(value)
                    else -> throw SzException("控制器方法参数自动转换器无法将字符串: $value 转换成指定数据类型: $type")
                }

            } catch (ex: Exception) {
                if (ex is SzException) throw ex
                throw SzException("参数 ${this.name} 的值不是一个有效的 ${this.type}")
            }
        }

        fun RoutingContext.queryParams(defaults: Map<String, String>): Map<String, String> {
            val result = this.request()
                .params()
                .map { Pair(it.key, it.value) }
                .toMap()
                .toMutableMap()

            val shouldAdd = defaults.filter { !result.containsKey(it.key) }

            result.putAll(shouldAdd)

            return result.toMap()
        }

        fun KFunction<*>.buildCallArgs(instance: Any, data: Map<String, String>): Map<KParameter, Any?> {
            val result = mutableMapOf<KParameter, Any?>()
            this.parameters.forEach {
                if (it.kind in listOf(KParameter.Kind.INSTANCE, KParameter.Kind.EXTENSION_RECEIVER)) {
                    result.put(it, instance)
                } else {
                    result.put(it, it.ParserVal(data))
                }
            }
            return result
        }

        fun parseFromFile(routeFile: File): List<ApiRoute> {
            val routes = routeFile.readLines()
                .map { it.trim() }
                .filter {
                    // 排除 注释行 和 空行
                    !it.startsWith("#") && !it.startsWith("//") && it.isNotBlank()
                }.map { parse(it) }

            return if (Application.inProductionMode) {
                //Production environment need exclude builtin route
                routes.filter { route ->
                    val methodFullName = "${route.controllerKClass.qualifiedName}.${route.controllerFun.name}"
                    builtinRouteControllers.contains(methodFullName).not()
                }
            } else {
                routes
            }
        }

        private fun parse(routeDef: String): ApiRoute {
            val routeRegex = """(GET|POST|HEAD)\s+(/\S*)\s+(\S+)\s*(\{.*\})?$""".toRegex()
            if (routeRegex.matches(routeDef)) {
                val parts = routeRegex.matchEntire(routeDef)!!.groupValues
                val method = parts[1].trim()
                val path = parts[2].trim()
                val controllerClassPath = parts[3].trim()

                val defaultsPart = parts[4].trim()
                val defaultArgs = if (defaultsPart.isEmpty()) {
                    mapOf()
                } else {
                    toStrMap(defaultsPart)
                }

                val controllerClassName = controllerClassPath.split(".").dropLast(1).joinToString(".")
                val controllerKClazz = loadKClass(controllerClassName)
                val controllerFunName = controllerClassPath.split(".").last()

                val funList = controllerKClazz.memberFunctions.filter { it.name == controllerFunName }
                if (funList.isEmpty()) {
                    throw SzException("在控制器: $controllerClassName 找不到方法: $controllerFunName")
                }
                if (funList.size > 1) {
                    throw SzException("控制器方法不能够重名/重载: $controllerClassPath")
                }

                return ApiRoute(method = HttpMethod.valueOf(method),
                    path = path,
                    controllerKClass = controllerKClazz,
                    controllerFun = funList.first(),
                    defaults = defaultArgs)

            } else {
                throw SzException("route definition syntax error: $routeDef")
            }
        }

        private fun loadKClass(className: String): KClass<*> {
            try {
                return Application.classLoader.loadClass(className).kotlin
            } catch (ex: ClassNotFoundException) {
                throw SzException("找不到控制器类: \"$className\"")
            }
        }
    }
}

internal data class Interceptor(val config: Any, val actionClass: KClass<*>) {

    fun chainedWith(httpContext: RoutingContext, delegateAction: Action<*>): Action<*> {
        val actionInstance = actionClass.createInstance() as Action<*>
        actionInstance.initialization(config, httpContext, delegateAction)
        return actionInstance
    }

    companion object {
        fun buildOf(controllerFun: KFunction<*>): List<Interceptor> {
            val interceptors = mutableListOf<Interceptor>()
            val annoInterceptors = controllerFun.annotations.filter {
                val ano = controllerFun.javaMethod!!.getAnnotation(it.annotationClass.java)
                return@filter ano.annotationClass.findAnnotation<WithAction>() != null
            }.map {
                val withAnno = it.annotationClass.findAnnotation<WithAction>()!!
                Interceptor(it, withAnno.value)
            }

            interceptors.addAll(globalInterceptors)
            interceptors.addAll(annoInterceptors)

            return interceptors.reversed()
        }

        private val globalInterceptors: List<Interceptor> by lazy {
            val interceptorsCfg = Application.config.getConfigList("app.httpServer.interceptors")

            interceptorsCfg.map { cfg ->
                val clazz = Application.classLoader.loadClass(cfg.getString("className"))
                if (ClassUtil.isTypeOf(clazz, GlobalInterceptorBase::class.java).not()) {
                    throw SzException("${clazz.name} is not sub class of GlobalInterceptorBase")
                }
                val interceptorCfg = JsonObject(cfg.getObject("config").unwrapped())
                Interceptor(interceptorCfg, clazz.kotlin)
            }
        }
    }
}