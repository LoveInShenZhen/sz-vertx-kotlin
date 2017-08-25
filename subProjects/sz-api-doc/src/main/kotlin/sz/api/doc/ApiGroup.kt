package sz.api.doc

import sz.scaffold.annotations.Comment
import sz.scaffold.ext.escapeMarkdown

//
// Created by kk on 17/8/24.
//
class ApiGroup(@Comment("api 分组名称") val groupName: String) {

    var apiInfoList: MutableList<ApiInfo> = mutableListOf()

    fun toMarkdownStr(str: String): String {
        return str.escapeMarkdown()
    }
}
