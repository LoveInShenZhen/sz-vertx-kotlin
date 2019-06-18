package sz.scaffold.tools.json

import com.fasterxml.jackson.databind.JsonNode

//
// Created by kk on 17/8/16.
//

fun Any.toJsonPretty(): String {
    return Json.toJsonStrPretty(this)
}

fun Any.toShortJson(): String {
    if (this is String) return this
    return Json.toJsonExcludeEmptyFields(this)
}

fun String.toJsonNode(): JsonNode {
    return Json.parse(this)
}

fun <A> JsonNode.toObj(clazz: Class<A>): A {
    return Json.fromJsonNode(this, clazz)
}

/**
 * 将JsonNode对象转换成指定类型的Bean对象
 */
inline fun <reified T> JsonNode.toBean(): T {
    return Json.fromJsonNode(this, T::class.java)
}

/**
 * 将json字符串转换成指定类型的Bean对象
 */
inline fun <reified T> String.toBean(): T {
    return Json.mapper.readValue(this, T::class.java)
}