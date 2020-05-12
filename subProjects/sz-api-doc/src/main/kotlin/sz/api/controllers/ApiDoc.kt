package sz.api.controllers

import sz.api.doc.DefinedApis
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.template.ResourceTemplate

//
// Created by kk on 17/8/24.
//
class ApiDoc : ApiController() {

    fun apiIndex(): String {
        val apis = DefinedApis(isJsonApi = true)
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiIndex.html", apis)
        contentType(ContentTypes.Html)
        return html
    }

    fun apiTest(apiUrl: String, httpMethod: String): String {
        val apiInfo = DefinedApis(isJsonApi = true)
            .groups.flatMap { it.apiInfoList }
            .find { it.path == apiUrl && it.httpMethod == httpMethod }
            ?: throw BizLogicException("route: $apiUrl 不存在或者http method 不匹配")
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiSample.html", apiInfo)
        contentType(ContentTypes.Html)
        return html
    }

    @Comment("非 api 的 http 路由列表")
    fun pageIndex(): String {
        val apis = DefinedApis(isJsonApi = false)
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiIndex.html", apis)
        contentType(ContentTypes.Html)
        return html
    }

    fun pageTest(apiUrl: String, httpMethod: String): String {
        val apiInfo = DefinedApis(isJsonApi = false)
            .groups.flatMap { it.apiInfoList }
            .find { it.path == apiUrl && it.httpMethod == httpMethod }
            ?: throw BizLogicException("route: $apiUrl 不存在或者http method 不匹配")
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/PageTest.html", apiInfo)
        contentType(ContentTypes.Html)
        return html
    }

    @Comment("返回api文档的markdown格式文本")
    fun apiDocMarkdown(): String {
        val apis = DefinedApis(isJsonApi = true)
        val markdown = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.md", apis)
        contentType(ContentTypes.Text)
        return markdown
    }

    @Comment("返回api文档的html格式文本")
    fun apiDocHtml(): String {
        val apis = DefinedApis(isJsonApi = true)
        val markdown = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.md", apis)
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.html", mapOf("api_markdown" to markdown))
        contentType(ContentTypes.Html)
        return html
    }

    @Comment("返回api接口信息")
    fun apiInfo(): ApiInfoReply {
        return ApiInfoReply.instance
    }
}