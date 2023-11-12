package sz.tools

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import sz.tools.Json

//
// Created by kk on 17/8/16.
//

fun Any.toJsonPretty(): String {
    if (this is String) return this
    return Json.toJsonStrPretty(this)
}

/**
 * 把对象序列化成单行json字符串, 并且忽略其中为空的字段(包括 null, 空字符串, 空列表, 空map)
 */
fun Any.toShortJson(): String {
    if (this is String) return this
    return Json.toJsonExcludeEmptyFields(this)
}

/**
 * 把对象序列化成单行json字符串
 */
fun Any.singleLineJson(): String {
    if (this is String) return this
    return Json.toJsonStr(this)
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
    return this.toJsonPretty().toBean()
}

/**
 * 将json字符串转换成指定类型的Bean对象
 */
inline fun <reified T> String.toBean(): T {
    return Json.mapper.readValue(this, object : TypeReference<T>() {})
}

inline fun <reified T> String.toBeanList(): List<T> {
    return Json.mapper.readValue(this, object : TypeReference<MutableList<T>>() {})
}

inline fun <reified T> String.toBeanMap(): Map<String, T> {
    return Json.mapper.readValue(this, object : TypeReference<MutableMap<String, T>>() {})
}

inline fun <reified KeyType, reified ValueType> String.toMutableMap(): MutableMap<KeyType, ValueType> {
    return Json.mapper.readValue(this, object : TypeReference<MutableMap<KeyType, ValueType>>() {})
}