package sz.scaffold.controller

import com.fasterxml.jackson.databind.JsonNode
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import jodd.exception.ExceptionUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import sz.scaffold.Application
import sz.scaffold.annotations.PostForm
import sz.scaffold.annotations.PostJson
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.profiler.ApiProfiler
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.coroutines.launchOnVertx
import sz.scaffold.dispatchers.IDispatcherFactory
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.io.StringReader
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

//
// Created by kk on 17/8/16.
//
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
            response.putHeader("Content-Type", "text/plain; charset=utf-8")
            response.end()
            return
        }

        val args = controllerFun.buildCallArgs(apiController, paramDatas)

        GlobalScope.launchOnVertx {
            coroutineScope {
                launch(workerDispatcher) {
                    val wrapperAction = buildWrappedAction(httpContext, args)
                    // 通过控制器方法的返回类型, 是否是ReplyBase或者其子类型, 来判断是否是 json api 方法
                    if (controllerFun.returnType.isSubtypeOf(ReplyBase::class.createType())) {
                        try {
                            val actionResult = ApiProfiler.runAction(wrapperAction)
                            if (isJsonpRequest(httpContext, actionResult)) {
                                onJsonp(httpContext, actionResult!!)
                            } else if (actionResult is ReplyBase) {
                                response.putHeader("Content-Type", "application/json; charset=utf-8")
                                response.write(actionResult.toJsonPretty())
                            }
                        } catch (ex: Exception) {
                            Logger.debug(ExceptionUtil.exceptionStackTraceToString(ex))
                            val reply = ReplyBase()
                            val reason = ExceptionUtil.findCause(ex, BizLogicException::class.java)
                            if (reason != null) {
                                reply.onError(reason)
                            } else {
                                reply.onError(ex)
                            }
                            if (httpContext.queryParams(mapOf()).containsKey("callback")) {
                                response.putHeader("Content-Type", "text/javascript; charset=utf-8")
                                val callback = httpContext.queryParams(mapOf()).getValue("callback")
                                val body = "$callback(${reply.toJsonPretty()});"
                                response.write(body)
                            } else {
                                response.putHeader("Content-Type", "application/json; charset=utf-8")
                                response.write(reply.toJsonPretty())
                            }
                        }

                    } else {
                        // 其他普通的 http 请求(非 api 请求)
                        try {
                            val result = ApiProfiler.runAction(wrapperAction)
                            onNormal(httpContext, result)

                        } catch (ex: Exception) {
                            response.putHeader("Content-Type", "text/plain; charset=utf-8")
                            val reason = if (ex.cause == null) ex else ex.cause
                            Logger.debug("非API请求处理发生异常, \n${ExceptionUtil.exceptionChainToString(reason)}")
                            response.end("${ex.message}\n\n${ExceptionUtil.exceptionChainToString(reason)}")
                        }
                    }

                    if (!response.ended()) {
                        response.end()
                    }
                }
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
        response.putHeader("Content-Type", "text/javascript; charset=utf-8")
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
            response.write(Json.toJsonStrPretty(result))
            response.putHeader("Content-Type", "application/json; charset=utf-8")
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
            return routeFile.readLines()
                .map { it.trim() }
                .filter {
                    // 排除 注释行 和 空行
                    !it.startsWith("#") && !it.startsWith("//") && it.isNotBlank()
                }.map { parse(it) }
        }

        fun parse(routeDef: String): ApiRoute {
            val routeRegex = """(GET|POST|HEAD)\s+(/\S*)\s+(\S+)\s*(\(.*\))?$""".toRegex()
            if (routeRegex.matches(routeDef)) {
                val parts = routeRegex.matchEntire(routeDef)!!.groupValues
                val method = parts[1].trim()
                val path = parts[2].trim()
                val controllerClassPath = parts[3].trim()
                val defaultArgs = parts[4].trim()
                    .removePrefix("(")
                    .removeSuffix(")")
                    .split(", ")
                    .map { it.trim() }
                    .joinToString("\n")

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

                val props = Properties()
                props.load(StringReader(defaultArgs))

                return ApiRoute(method = HttpMethod.valueOf(method),
                    path = path,
                    controllerKClass = controllerKClazz,
                    controllerFun = funList.first(),
                    defaults = props.map { Pair(it.key.toString(), it.value.toString()) }.toMap())

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


        val workerDispatcher: CoroutineDispatcher by lazy {
            val factory = Class.forName(Application.config.getString("app.httpServer.dispatcher.factory")).newInstance() as IDispatcherFactory
            factory.build()
        }
    }
}

internal data class Interceptor(val configAnno: Annotation, val actionClass: KClass<*>) {

    fun chainedWith(httpContext: RoutingContext, delegateAction: Action<*>): Action<*> {
        val actionInstance = actionClass.createInstance() as Action<*>
        actionInstance.init(configAnno, httpContext, delegateAction)
        return actionInstance
    }

    companion object {
        fun buildOf(controllerFun: KFunction<*>): List<Interceptor> {
            return controllerFun.annotations.filter {
                val ano = controllerFun.javaMethod!!.getAnnotation(it.annotationClass.java)
                return@filter ano.annotationClass.findAnnotation<WithAction>() != null
            }.reversed().map {
                val withAnno = it.annotationClass.findAnnotation<WithAction>()!!
                Interceptor(it, withAnno.value)
            }
        }
    }
}