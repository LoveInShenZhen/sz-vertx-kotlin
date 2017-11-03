package sz.scaffold.tools.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import jodd.datetime.JDateTime
import java.io.IOException

//
// Created by kk on 2017/11/3.
//
class JDateTimeWithMsJsonDeserializer: JsonDeserializer<JDateTime>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JDateTime {
        val v = p.readValueAs(String::class.java)
        if (v.length == 19) {
            return JDateTime(v, "YYYY-MM-DD hh:mm:ss")
        } else {
            return JDateTime(v, "YYYY-MM-DD hh:mm:ss.mss")
        }
    }
}