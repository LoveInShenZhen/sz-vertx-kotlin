package sz.scaffold.tools.json

import com.fasterxml.jackson.core.type.TypeReference
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