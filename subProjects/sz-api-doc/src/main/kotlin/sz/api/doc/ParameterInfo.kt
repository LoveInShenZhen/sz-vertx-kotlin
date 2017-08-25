package sz.api.doc

import sz.scaffold.annotations.Comment
import sz.scaffold.ext.escapeMarkdown

//
// Created by kk on 17/8/24.
//
data class ParameterInfo(@Comment("方法参数名称")
                         var name: String = "",

                         @Comment("参数描述")
                         var desc: String = "",

                         @Comment("参数的数据类型")
                         var type: String = "") {

    fun toMarkdownStr(str: String): String {
        return str.escapeMarkdown()
    }
}