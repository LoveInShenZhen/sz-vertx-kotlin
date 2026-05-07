package sz.scaffold.tools.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass

//
// Created by kk on 17/8/16.
//
object Json {

    val mapper: ObjectMapper = ObjectMapper()
    val excludeEmptyMapper = ObjectMapper()

    init {
        val JDateTimeModule = SimpleModule("CustomTypeModule")

        mapper.registerKotlinModule()
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())
            .registerModule(JDateTimeModule)

        excludeEmptyMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        excludeEmptyMapper.registerKotlinModule()
            .registerModule(Jdk8Module())
            .registerModule(JavaTimeModule())
            .registerModule(JDateTimeModule)

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

    /**
     * 将json字符串转换为ObjectNode, 调用方保证json字符串是合法的,且是object类型
     */
    fun parseToObjectNode(src: String): ObjectNode {
        try {
            return mapper.readTree(src) as ObjectNode
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }
    }

    /**
     * 将json字符串转换为ArrayNode, 调用方保证json字符串是合法的,且是array类型
     */
    fun parseToArrayNode(src: String): ArrayNode {
        try {
            return mapper.readTree(src) as ArrayNode
        } catch (t: Throwable) {
            throw RuntimeException(t)
        }
    }

    fun toJsonStr(obj: Any): String {
        return Json.toJson(obj).toString()
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

    fun generateSchema(clazz: KClass<*>): JsonSchema {
        val schemaGen = JsonSchemaGenerator(mapper)
        return schemaGen.generateSchema(clazz.java)
    }

    fun toStrMap(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val jsonNode = parse(jsonStr)
        jsonNode.fields().forEach { map.put(it.key, it.value.asText()) }
        return map
    }
}