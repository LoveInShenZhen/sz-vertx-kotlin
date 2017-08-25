package sz.api.controllers

import sz.api.doc.DefinedApis
import sz.scaffold.controller.ApiController
import sz.scaffold.tools.template.ResourceTemplate

//
// Created by kk on 17/8/24.
//
class ApiDoc : ApiController() {

    fun apiIndex() : String {
        val apis = DefinedApis(this.httpContext.request().host())
        val html = ResourceTemplate.Process(DefinedApis::class.java, "/ApiDocTemplates/ApiIndex.html", apis)
        contentType("text/html; charset=UTF-8")
        return html
    }

    fun apiTest(apiUrl: String) : String {
        val apiInfo = DefinedApis(this.httpContext.request().host())
                .groups.flatMap { it.apiInfoList }
                .find { it.url == apiUrl }!!
        val html = ResourceTemplate.Process(DefinedApis::class.java, "/ApiDocTemplates/ApiSample.html", apiInfo)
        contentType("text/html; charset=UTF-8")
        return html
    }

}