package sz.api.doc

import sz.api.controllers.ApiDoc
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.annotations.PostJson
import sz.scaffold.controller.ApiRoute
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

//
// Created by kk on 17/8/24.
//
@Suppress("MemberVisibilityCanBePrivate")
class DefinedApis(@Comment("是否是 json api") val isJsonApi: Boolean = true) {

    var groups: MutableList<ApiGroup> = mutableListOf()

    init {
        Application.loadApiRouteFromRouteFiles().filter {
            it.isJsonApi() == isJsonApi && it.controllerKClass != ApiDoc::class
        }.forEach {
            addApi(it)
        }
    }

    private fun apiGrouByName(name: String): ApiGroup {
        val group = groups.find { it.groupName == name }
        return if (group != null) {
            group
        } else {
            val newGroup = ApiGroup(name)
            groups.add(newGroup)
            newGroup
        }
    }

    private fun addApi(apiRoute: ApiRoute) {
        val apiInfo = apiRoute.buildApiInfo()
        val group = apiGrouByName(apiInfo.groupName)
        group.apiInfoList.add(apiInfo)
    }

}

fun ApiRoute.buildApiInfo(): ApiInfo {
    var httpMethod = this.method.name
    if (httpMethod == "POST") {
        httpMethod = ApiInfo.PostForm
//        if (this.controllerFun.findAnnotation<PostForm>() != null) {
//            httpMethod = ApiInfo.PostForm
//        }
        if (this.controllerFun.findAnnotation<PostJson>() != null) {
            httpMethod = ApiInfo.PostJson
        }
    }

    return ApiInfo(
        path = this.path,
        httpMethod = httpMethod,
        controllerClass = this.controllerKClass.java.name,
        methodName = this.controllerFun.name,
        replyKClass = this.returnType().jvmErasure,
        postDataKClass = this.postBodyClass(),
        is_json_api = this.isJsonApi()
    )
}