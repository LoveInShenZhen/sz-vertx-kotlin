package sz.scaffold.controller

import com.fasterxml.jackson.databind.JsonNode
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.javaMethod

//
// Created by kk on 17/8/16.
//
class ApiRoute(val method: ApiHttpMethod,
               val path: String,
               val controllerKClass: KClass<*>,
               val controllerFun: KFunction<*>,
               val defaults: Map<String, String>) {

    fun addToRoute(router: Router) {
        router.route(method.httpMethod(), path).blockingHandler { routingContext ->
            callApi(routingContext)
        }
    }

    private fun callApi(httpContext: RoutingContext) {
        val controller = controllerKClass.createInstance()
        val apiController = controller as ApiController
        apiController.init(httpContext)

        val paramDatas = httpContext.queryParams(defaults)
        val args = controllerFun.buildCallArgs(apiController, paramDatas)

        val response = httpContext.response()

        val wrapperAction = buildWrappedAction(httpContext, args)

        // 通过控制器方法的返回类型, 是否是ReplyBase或者其子类型, 来判断是否是 json api 方法
        if (controllerFun.returnType.isSubtypeOf(ReplyBase::class.createType())) {
            val result = wrapperAction.call() as ReplyBase

            response.putHeader("Content-Type", "application/json; charset=utf-8")
            response.write(result.toJsonPretty())
        } else {
            // 其他普通的 http 请求(非 api 请求)

            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.isChunked = true

            val result = wrapperAction.call()
            Logger.debug("其他普通的 http 请求(非 api 请求)")

            if (result != null && result != Unit) {
                response.write(result.toString())
            }

            if (result is ReplyBase || result is JsonNode) {
                response.putHeader("Content-Type", "application/json; charset=utf-8")
            }
        }

        if (!response.ended()) {
            response.end()
        }
    }

    private fun buildWrappedAction(httpContext: RoutingContext, args: Map<KParameter, Any?>) : Action<*> {
        val actionAnnos = controllerFun.annotations.filter {
            val ano = controllerFun.javaMethod!!.getAnnotation(it.annotationClass.java)
            return@filter ano.annotationClass.findAnnotation<WithAction>() != null
        }.reversed()
        var resultAction : Action<*> = Action.WrapperAction<Any> {
            return@WrapperAction controllerFun.callBy(args)
        }

        actionAnnos.forEach {
            val withAnno = it.annotationClass.findAnnotation<WithAction>()!!
            val actionClass = withAnno.value
            val actionInstance = actionClass.createInstance() as Action<*>
            actionInstance.init(it, httpContext, resultAction)

            resultAction = actionInstance
        }

        return resultAction
    }

    companion object {
        private fun KParameter.ParserVal(data: Map<String, String>): Any? {
            if (this.kind != KParameter.Kind.VALUE) {
                throw RuntimeException("参数种类为: ${this.kind}")
            }
            val value = data[this.name!!]
            if (value == null) {
                if (this.type.isMarkedNullable) {
                    return null
                } else {
                    throw RuntimeException("缺少参数: ${this.name} : ${this.type}")
                }
            }

            // 根据参数类型, 从字符串转换成对应的类型, 并且只支持下列类型
            try {
                val result: Any? = when (this.type.toString().replace("?", "")) {
                    "kotlin.String" -> value
                    "kotlin.Int" -> value.toInt()
                    "kotlin.Short" -> value.toShort()
                    "kotlin.Byte" -> value.toByte()
                    "kotlin.Long" -> value.toLong()
                    "kotlin.Float" -> value.toFloat()
                    "kotlin.Double" -> value.toDouble()
                    "kotlin.Boolean" -> value.toBoolean()
                    "java.math.BigDecimal" -> BigDecimal(value)
                    else -> throw RuntimeException("控制器方法参数自动转换器无法将字符串: $value 转换成指定数据类型: ${this.type}")

                }
                return result

            } catch (ex: Exception) {
                throw RuntimeException("参数 ${this.name} 的值不是一个有效的 ${this.type}")
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
    }
}
