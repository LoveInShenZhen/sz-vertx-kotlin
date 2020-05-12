package sz.api.doc

import sz.scaffold.annotations.Comment
import sz.scaffold.ext.escapeMarkdown
import java.lang.StringBuilder

//
// Created by kk on 17/8/24.
//
@Suppress("CanBePrimaryConstructorProperty")
class ParameterInfo(name: String, desc: String, type: String, required: Boolean = true, defaultValue: String = "") {

    @Comment("方法参数名称")
    var name: String = name

    @Comment("参数描述")
    var desc: String = desc

    @Comment("参数的数据类型")
    var type: String = type

    @Comment("是否为必填参数")
    var required: Boolean = required

    @Comment("为可选参数时的默认值")
    var defaultValue = defaultValue

    fun toMarkdownStr(str: String): String {
        return str.escapeMarkdown()
    }

    fun summary(): String {
        val sb = StringBuilder()
        sb.append("$type, $desc")
        if (required) {
            sb.append(", [必填]")
        } else {
            sb.append(", [可选], [默认值: $defaultValue]")
        }

        return sb.toString()
    }
}