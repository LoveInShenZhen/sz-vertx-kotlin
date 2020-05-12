package sz.api.controllers

import sz.api.doc.ApiGroup
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.reply.ReplyBase

//
// Created by kk on 2020/5/12.
//
class ApiInfoReply : ReplyBase() {

    @Comment("json api 接口信息")
    var json_api_groups: List<ApiGroup>? = null

    @Comment("非json api的接口信息")
    var non_api_groups: List<ApiGroup>? = null
}