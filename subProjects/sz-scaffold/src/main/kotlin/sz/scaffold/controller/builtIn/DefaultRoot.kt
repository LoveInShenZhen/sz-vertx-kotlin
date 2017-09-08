package sz.scaffold.controller.builtIn

import sz.scaffold.controller.ApiController

//
// Created by kk on 17/9/8.
//
class DefaultRoot : ApiController() {

    fun hello() : String {
        this.contentType("text/plain")
        return "hello"
    }
}