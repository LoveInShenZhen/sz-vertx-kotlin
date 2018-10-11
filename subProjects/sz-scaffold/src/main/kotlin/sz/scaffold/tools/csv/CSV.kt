package sz.scaffold.tools.csv

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jodd.datetime.JDateTime
import sz.scaffold.tools.json.JDateTimeJsonDeserializer
import sz.scaffold.tools.json.JDateTimeJsonSerializer
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.io.FileWriter

//
// Created by kk on 2018/10/11.
//
object CSV {

    val mapper = CsvMapper()

    init {
        val JDateTimeModule = SimpleModule("CustomTypeModule")
        JDateTimeModule.addSerializer(JDateTime::class.java, JDateTimeJsonSerializer())
        JDateTimeModule.addDeserializer(JDateTime::class.java, JDateTimeJsonDeserializer())

        mapper.registerKotlinModule()
                .registerModule(Jdk8Module())
                .registerModule(JavaTimeModule())
                .registerModule(JDateTimeModule)
    }

    fun saveToFile(csvFilePath: String, beans: List<*>, clazz: Class<*>, append: Boolean = false) {
        val csvSchema = mapper.schemaFor(clazz)
        val destFile = File(csvFilePath)
        val needWriteHeader = append.not() || destFile.length() == 0L

        val objw = if (needWriteHeader) {
            mapper.writerFor(clazz).with(csvSchema.withHeader()).writeValues(destFile.bufferedWriter())
        } else {
            mapper.writerFor(clazz).with(csvSchema).writeValues(destFile.bufferedWriter())
        }

        objw.use {
            objw.writeAll(beans)
        }
    }

}