package sz.scaffold.tools.csv

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jodd.datetime.JDateTime
import sz.scaffold.tools.json.JDateTimeJsonDeserializer
import sz.scaffold.tools.json.JDateTimeJsonSerializer
import java.io.File
import java.io.FileOutputStream

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

    fun saveToFile(csvFile: File, beans: List<*>, clazz: Class<*>, append: Boolean = false) {
        val csvSchema = mapper.schemaFor(clazz)
        val needWriteHeader = append.not() || csvFile.exists().not() || csvFile.length() == 0L

        val fos = FileOutputStream(csvFile, append).buffered()

        val objw = if (needWriteHeader) {
            mapper.writerFor(clazz).with(csvSchema.withHeader()).writeValues(fos)
        } else {
            mapper.writerFor(clazz).with(csvSchema).writeValues(fos)
        }

        objw.use {
            objw.writeAll(beans)
        }
    }

    inline fun <reified T : Any> saveToFile(csvFilePath: String, beans: List<T>, append: Boolean = false) {
        saveToFile(File(csvFilePath), beans, T::class.java, append)
    }

    inline fun <reified T : Any> saveToFile(csvFileh: File, beans: List<T>, append: Boolean = false) {
        saveToFile(csvFileh, beans, T::class.java, append)
    }

    inline fun <reified T : Any> loadFromFile(csvFile: File): List<T> {
        val csvSchema = mapper.schemaFor(T::class.java).withHeader()

        val it = mapper.readerFor(T::class.java).with(csvSchema).readValues<T>(csvFile.bufferedReader())
        return it.readAll()
    }

    inline fun <reified T : Any> loadFromFile(csvFilePath: String): List<T> {
        return loadFromFile(File(csvFilePath))
    }

}