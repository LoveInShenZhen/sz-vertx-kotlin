package sz.scaffold.eventbus

import io.netty.util.CharsetUtil
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.toShortJson

//
// Created by kk on 2019/11/25.
//
class BeanMessageCodec<BeanType : Any>(private val beanClass: Class<BeanType>) : MessageCodec<BeanType, BeanType> {

    override fun decodeFromWire(pos: Int, buffer: Buffer): BeanType {
        val length = buffer.getInt(pos)
        val startPos = pos + 4
        val bytes = buffer.getBytes(startPos, startPos + length)
        val jsonStr = String(bytes, CharsetUtil.UTF_8)
        return Json.fromJsonString(jsonStr, beanClass)
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer, s: BeanType) {
        val jsonStr = s.toShortJson()
        val strBytes: ByteArray = jsonStr.toByteArray(CharsetUtil.UTF_8)
        buffer.appendInt(strBytes.size)
        buffer.appendBytes(strBytes)
    }

    override fun transform(s: BeanType): BeanType {
        return s
    }

    override fun name(): String {
        return beanClass.name
    }

}