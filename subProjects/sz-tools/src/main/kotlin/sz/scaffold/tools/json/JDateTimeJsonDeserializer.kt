package sz.scaffold.tools.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import jodd.datetime.JDateTime

import java.io.IOException

/**
 * Created by kk on 16/2/21.
 */
class JDateTimeJsonDeserializer : JsonDeserializer<JDateTime>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JDateTime {
        val v = p.readValueAs(String::class.java)
        return JDateTime(v, "YYYY-MM-DD hh:mm:ss")
    }
}
