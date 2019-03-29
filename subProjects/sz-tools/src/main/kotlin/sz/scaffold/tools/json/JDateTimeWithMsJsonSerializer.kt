package sz.scaffold.tools.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import jodd.datetime.JDateTime
import java.io.IOException

//
// Created by kk on 2017/11/3.
//
class JDateTimeWithMsJsonSerializer: JsonSerializer<JDateTime>() {

    @Throws(IOException::class)
    override fun serialize(value: JDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString("YYYY-MM-DD hh:mm:ss.mss"))
    }
}