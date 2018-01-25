package sz.scaffold.tools.xml

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jodd.datetime.JDateTime
import sz.scaffold.tools.json.JDateTimeJsonDeserializer
import sz.scaffold.tools.json.JDateTimeJsonSerializer

//
// Created by kk on 2018/1/23.
//
object Xml {

    val mapper: XmlMapper

    init {
        val JDateTimeModule = SimpleModule("CustomTypeModule")
        JDateTimeModule.addSerializer(JDateTime::class.java, JDateTimeJsonSerializer())
        JDateTimeModule.addDeserializer(JDateTime::class.java, JDateTimeJsonDeserializer())

        mapper = XmlMapper()
        mapper.registerKotlinModule()
                .registerModule(Jdk8Module())
                .registerModule(JavaTimeModule())
                .registerModule(JDateTimeModule)
    }

    fun toXml(data: Any): String {
        return mapper.writeValueAsString(data)
    }

    fun toXmlPretty(data: Any): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data)
    }

    fun <A> fromXml(xmlStr: String, clazz: Class<A>): A {
        return mapper.readValue(xmlStr, clazz)
    }

}