package myquant.tools

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

//
// Created by kk on 17/8/16.
//
object Json {

    val mapper: ObjectMapper = ObjectMapper()
    val excludeEmptyMapper = ObjectMapper()

    init {
        mapper.registerKotlinModule()
            .registerModule(JavaTimeModule())

        excludeEmptyMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        excludeEmptyMapper.registerKotlinModule()
            .registerModule(JavaTimeModule())
    }

    fun toJson(data: Any): JsonNode {
        try {
            return mapper.valueToTree(data)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun parse(src: String): JsonNode {
        try {
            return mapper.readTree(src)
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }
    }

    fun toJsonStr(obj: Any): String {
        return toJson(obj).toString()
    }

    fun toJsonStrPretty(obj: Any): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }

    fun toJsonExcludeEmptyFields(obj: Any): String {
        return excludeEmptyMapper.writeValueAsString(obj)
    }

    fun toJsonExcludeEmptyFieldsPretty(obj: Any): String {
        return excludeEmptyMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }

    fun formatJson(jsonStr: String): String {
        val jsonNode = parse(jsonStr)
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun <A> fromJsonString(jsonStr: String, clazz: Class<A>): A {
        try {
            return mapper.readValue<A>(jsonStr, clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    inline fun <reified A : Any> fromJsonString(jsonStr: String): A {
        try {
            return mapper.readValue<A>(jsonStr, A::class.java)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun <A> fromJsonNode(jsonNode: JsonNode, clazz: Class<A>): A {
        try {
            return mapper.treeToValue(jsonNode, clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun toStrMap(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val jsonNode = parse(jsonStr)
        jsonNode.fields().forEach { map.put(it.key, it.value.asText()) }
        return map
    }

    fun createArrayNode(): ArrayNode {
        return this.mapper.createArrayNode()
    }

    fun createObjectNode(): ObjectNode {
        return this.mapper.createObjectNode()
    }
}