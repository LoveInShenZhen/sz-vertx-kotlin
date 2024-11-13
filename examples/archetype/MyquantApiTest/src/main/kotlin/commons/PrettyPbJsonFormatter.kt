package commons

import com.fasterxml.jackson.core.JsonGenerator
import com.google.protobuf.Message
import com.googlecode.protobuf.format.JsonJacksonFormat
import java.io.OutputStream

//
// Created by drago on 2024/11/6 周三.
//
class PrettyPbJsonFormatter : JsonJacksonFormat() {
    override fun createGenerator(output: OutputStream?): JsonGenerator {
        val jsonGenerator = super.createGenerator(output)
        jsonGenerator.useDefaultPrettyPrinter()
        return jsonGenerator
    }
}

val pbJsonFormatter = PrettyPbJsonFormatter()

fun Message.toJsonStr():String {
    return pbJsonFormatter.printToString(this)
}