package sz.api.controllers

import sz.api.doc.DefinedApis
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.template.ResourceTemplate

//
// Created by kk on 17/8/24.
//
class ApiDoc : ApiController() {

    fun apiIndex(): String {
        val apis = DefinedApis(this.httpContext.request().host())
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiIndex.html", apis)
        contentType(ContentTypes.Html)
        return html
    }

    fun apiTest(apiUrl: String, httpMethod: String): String {
        val apiInfo = DefinedApis(this.httpContext.request().host())
            .groups.flatMap { it.apiInfoList }
            .find { it.url == apiUrl && it.httpMethod == httpMethod }!!
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiSample.html", apiInfo)
        contentType(ContentTypes.Html)
        return html
    }

    @Comment("非 api 的 http 路由列表")
    fun pageIndex(): String {
        val apis = DefinedApis(this.httpContext.request().host(), false)
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiIndex.html", apis)
        contentType(ContentTypes.Html)
        return html
    }

    fun pageTest(apiUrl: String, httpMethod: String): String {
        val apiInfo = DefinedApis(this.httpContext.request().host(), false)
            .groups.flatMap { it.apiInfoList }
            .find { it.url == apiUrl && it.httpMethod == httpMethod }!!
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/PageTest.html", apiInfo)
        contentType(ContentTypes.Html)
        return html
    }

    @Comment("返回api文档的markdown格式文本")
    fun apiDocMarkdown(): String {
        val apis = DefinedApis(this.httpContext.request().host())
        val markdown = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.md", apis)
        contentType(ContentTypes.Text)
        return markdown
    }

    @Comment("返回api文档的html格式文本")
    fun apiDocHtml(): String {
        val apis = DefinedApis(this.httpContext.request().host())
        val markdown = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.md", apis)
        val html = ResourceTemplate.process(DefinedApis::class.java, "/ApiDocTemplates/ApiDoc.html", mapOf("api_markdown" to markdown))
        contentType(ContentTypes.Html)
        return html
    }
}