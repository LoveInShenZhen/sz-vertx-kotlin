package sz.api.doc

import com.fasterxml.jackson.annotation.JsonIgnore
import sz.api.controllers.ApiDoc
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.ext.escapeMarkdown
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

//
// Created by kk on 17/8/24.
//
class ApiInfo
constructor(
        val host: String = "localhost:9000",

        @Comment("API url")
        val url: String,

        @Comment("API http method: GET or POST")
        val httpMethod: String,

        @Comment("API 对应的 Controller 类名称")
        val controllerClass: String,

        @Comment("API 对应的 Controller 类下的方法名称")
        val methodName: String,

        replyKClass: KClass<*>,

        postDataKClass: KClass<*>?) {

    @Comment("返回Replay 对应的 java class name")
    var replyClass: String = ""

    @Comment("POST 方法时, Form 表单 or JSON 对应的 类名称")
    var postDataClass: String = ""

    @Comment("POST 方法时, Form 表单 or JSON 对应的 Sample")
    var postDataSample: String = ""

    @Comment("API 描述")
    var apiComment: String = ""

    @Comment("返回Replay的描述信息")
    var replyInfo: FieldSchema

    @Comment("返回结果样例")
    var replySampleData: String = ""

    @Comment("API 所有参数的描述")
    var params: List<ParameterInfo> = emptyList()

    init {
        replyClass = replyKClass.javaObjectType.name
        postDataClass = if (postDataKClass == null) {
            ""
        } else {
            postDataKClass.javaObjectType.name
        }

        replyInfo = FieldSchema()
        replyInfo.level = 0
        replyInfo.name = "reply"
        replyInfo.desc = ""
        replyInfo.type = JsonDataType.OBJECT.typeName

        postDataSample = if (postDataKClass != null && IsPostJsonApi()) {
            SampleJsonData(postDataKClass)
        } else {
            ""
        }

        replySampleData = SampleJsonData(replyKClass)

        analyse()
    }

    fun toMarkdownStr(str: String): String {
        return str.escapeMarkdown()
    }

    fun groupName(): String {
        val controllerClazz = Class.forName(this.controllerClass)
        val anno = controllerClazz.getAnnotation(Comment::class.java)
        return anno?.value ?: this.controllerClass
    }

    private fun analyse() {

        analyseMethod()

        analyseReply()
    }

    private fun analyseMethod() {
        // 分析 controller 方法信息
        val controllerKClazz = Class.forName(this.controllerClass).kotlin
        val method = controllerKClazz.functions.find { it.name == this.methodName }
        val commentAnno = method!!.annotations.find { it is Comment }
        if (commentAnno != null && commentAnno is Comment) {
            this.apiComment = commentAnno.value
        }

        this.params = method.parameters
                .filter { it.name != null }
                .map {
                    var paramDesc = ""
                    val paramComment = it.annotations.find { it is Comment }
                    if (paramComment != null && paramComment is Comment) {
                        paramDesc = paramComment.value
                    }
                    ParameterInfo(name = it.name!!,
                            desc = paramDesc,
                            type = it.type.javaType.typeName.split(".").last())
                }

//        val jsonApiAnno = method.annotations.find { it is JsonApi } as JsonApi
//
//        if (postDataClass.isNotBlank()) {
//            if (jsonApiAnno.ApiMethodType == ApiInfo.PostForm) {
//                // todo 构造 form 表单
//            }
//
//            if (jsonApiAnno.ApiMethodType == ApiInfo.PostJson) {
//                // todo 构造 post 的 JSON 字符串
//            }
//        }

    }

    private fun analyseReply() {
        // 分析返回的 reply 的信息
        FieldSchema.resolveFields(Class.forName(this.replyClass).kotlin, replyInfo)
    }

    fun TestPage(): String {
        return "http://$host${pathOfTestPage()}?apiUrl=${this.url}"
    }

    fun IsGetJsonApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.Get, ignoreCase = true)
    }

    fun IsPostJsonApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.PostJson, ignoreCase = true)
    }

    fun IsPostFormApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.PostForm, ignoreCase = true)
    }

    fun PostFormFieldInfos(): List<ParameterInfo> {
        if (this.IsPostFormApi()) {
            return Class.forName(this.postDataClass).kotlin.memberProperties
                    .filter { it.visibility == KVisibility.PUBLIC && it.findAnnotation<JsonIgnore>() == null }
                    .map {
                        var paramDesc = ""
                        val paramComment = it.annotations.find { it is Comment }
                        if (paramComment != null && paramComment is Comment) {
                            paramDesc = paramComment.value
                        }
                        ParameterInfo(name = it.name,
                                desc = paramDesc,
                                type = it.returnType.javaType.typeName.split(".").last())
                    }
        } else {
            return emptyList()
        }
    }

    fun PostJsonFieldInfos(): List<ParameterInfo> {
        if (this.IsPostJsonApi()) {
            return Class.forName(this.postDataClass).kotlin.memberProperties
                    .filter { it.visibility == KVisibility.PUBLIC && it.findAnnotation<JsonIgnore>() == null }
                    .map {
                        var paramDesc = ""
                        val paramComment = it.annotations.find { it is Comment }
                        if (paramComment != null && paramComment is Comment) {
                            paramDesc = paramComment.value
                        }
                        ParameterInfo(name = it.name,
                                desc = paramDesc,
                                type = it.returnType.javaType.typeName.split(".").last())
                    }
        } else {
            return emptyList()
        }
    }

    companion object {

        const val PostJson = "POST JSON"
        const val PostForm = "POST FORM"
        const val Get = "GET"

        private fun defaultSampleJson(kClass: KClass<*>): String {
            try {
                val sampleObj = kClass.java.newInstance()
                return sampleObj.toJsonPretty()
            } catch (ex: Exception) {
                Logger.debug(ex.cause.toString())
                return ""
            }
        }

        fun SampleJsonData(kClass: KClass<*>): String {
            if (kClass == Any::class) {
                return "没有在@PostJson 注解里指定 PostJson 对应的Class, 请自行脑补需要Post的 json"
            }
            val sampleDataFunc = kClass.memberFunctions
                    .find { it.name == "SampleData" } ?: return "请在 ${kClass.qualifiedName} 实现 fun SampleData() {...}方法\n${defaultSampleJson(kClass)}"

            if (sampleDataFunc.parameters.size != 1) {
                return "请在 ${kClass.qualifiedName} 实现 fun SampleData() {...}方法(无参数)"
            }
            val sampleObj = kClass.java.newInstance()
            if (sampleObj != null) {
                sampleDataFunc.call(sampleObj)
                return sampleObj.toJsonPretty()
            } else {
                return "${kClass.qualifiedName} 需要提供无参数的构造函数"
            }
        }

        private var testPagePath = ""
        fun pathOfTestPage(): String {
            if (testPagePath.isBlank()) {
                testPagePath = Application.loadApiRouteFromRouteFiles().find { it.controllerKClass == ApiDoc::class && it.controllerFun.name == "apiTest" }!!.path
            }
            return testPagePath
        }
    }
}