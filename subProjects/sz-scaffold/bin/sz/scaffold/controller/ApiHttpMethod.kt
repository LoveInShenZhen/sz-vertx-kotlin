package sz.scaffold.controller

import io.vertx.core.http.HttpMethod

//
// Created by kk on 17/8/16.
//
enum class ApiHttpMethod {
    GET,
    POST_FORM,
    POST_JSON;

    fun httpMethod(): HttpMethod {
        when (this) {
            GET -> return HttpMethod.GET
            POST_FORM -> return HttpMethod.POST
            POST_JSON -> return HttpMethod.POST
            else -> return HttpMethod.GET
        }
    }
}