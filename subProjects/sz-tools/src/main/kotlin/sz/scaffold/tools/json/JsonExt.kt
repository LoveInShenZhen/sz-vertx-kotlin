package sz.scaffold.tools.json

import com.fasterxml.jackson.databind.JsonNode

//
// Created by kk on 17/8/16.
//

fun Any.toJsonPretty(): String {
    return Json.toJsonStrPretty(this)
}

fun Any.toShortJson(): String {
    return Json.toJsonExcludeEmptyFields(this)
}

fun String.toJsonNode(): JsonNode {
    return Json.parse(this)
}

fun <A> JsonNode.toObj(clazz: Class<A>): A {
    return Json.fromJsonNode(this, clazz)
}