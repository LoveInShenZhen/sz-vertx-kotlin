package controllers

import sz.scaffold.controller.ApiController
import sz.scaffold.controller.reply.ReplyBase

class Sample : ApiController() {

    fun kktest():ReplyBase {
        val reply = ReplyBase()

        return reply
    }
}