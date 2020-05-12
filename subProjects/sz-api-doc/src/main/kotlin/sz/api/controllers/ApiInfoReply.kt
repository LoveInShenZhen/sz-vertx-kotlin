package sz.api.controllers

import sz.api.doc.ApiGroup
import sz.api.doc.DefinedApis
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.reply.ReplyBase

//
// Created by kk on 2020/5/12.
//
class ApiInfoReply : ReplyBase() {

    @Comment("json api 接口信息")
    var json_api_groups: List<ApiGroup> = listOf()

    @Comment("非json api的接口信息")
    var non_api_groups: List<ApiGroup> = listOf()

    fun load() {
        json_api_groups = DefinedApis(isJsonApi = true).groups
        non_api_groups = DefinedApis(isJsonApi = false).groups
    }

    companion object {

        val instance : ApiInfoReply by lazy {
            ApiInfoReply().apply { load() }
        }

    }
}