import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.util.*

//
// Created by drago on 2024/7/29 周一.
//
class tool_api_test {

    val log: Logger = LoggerFactory.getLogger("TEST")

    @Test
    @DisplayName("测试生成token")
    fun GenTokenTest() {
        val h = MessageDigest.getInstance("SHA-1")
        val uid = UUID.randomUUID().toString()
        val d = h.digest(uid.toByteArray())
        val token = d.map { String.format("%02x", it) }.take(40).joinToString("")

        log.info(token)
        log.info("${token.length}")

        log.info(h.toString())
    }
}