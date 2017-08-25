package sz.scaffold.tools.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jodd.datetime.JDateTime
import kotlin.reflect.KClass

//
// Created by kk on 17/8/16.
//
object Json {

    val mapper: ObjectMapper = ObjectMapper()

    init {
        val JDateTimeModule = SimpleModule("CustomTypeModule")
        JDateTimeModule.addSerializer(JDateTime::class.java, JDateTimeJsonSerializer())
        JDateTimeModule.addDeserializer(JDateTime::class.java, JDateTimeJsonDeserializer())

        mapper.registerKotlinModule()
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

    fun toJsonStr(obj: Any): String {
        return Json.toJson(obj).toString()
    }

    fun toJsonStrPretty(obj: Any): String {
        val jsonNode = Json.toJson(obj)
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun formatJson(jsonStr: String): String {
        val jsonNode = Json.parse(jsonStr)
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }

    fun <A> fromJsonString(jsonStr: String, clazz: Class<A>): A {
        val node = Json.parse(jsonStr)
        try {
            return mapper.treeToValue(node, clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun generateSchema(clazz: KClass<*>) : JsonSchema {
        val schemaGen = JsonSchemaGenerator(mapper)
        return schemaGen.generateSchema(clazz.java)
    }

    fun toStrMap(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val jsonNode = Json.parse(jsonStr)
        jsonNode.fields().forEach { map.put(it.key, it.value.asText()) }
        return map
    }
}