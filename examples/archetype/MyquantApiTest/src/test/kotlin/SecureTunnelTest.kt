import io.grpc.ManagedChannelBuilder
import myquant.rpc.client.SecureTunnel
import myquant.toJsonStr
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.security.Security

//
// Created by drago on 2024/11/19 周二.
//

@DisplayName("单元测试")
class SecureTunnelTest {
    val log = LoggerFactory.getLogger("TEST")

    @DisplayName("以安全加密的方式获取密文token")
    @Test
    fun getEncryptedTokenTest() {
        val channel = ManagedChannelBuilder.forAddress("120.78.94.151", 8201)
            .usePlaintext()
            .enableRetry()
            .maxRetryAttempts(3)
            .maxInboundMessageSize(1024 * 1024 * 256)
            .build()

        val secureTunnel = SecureTunnel(gmadminChannel = channel)

        val rsp = secureTunnel.getEncryptedToken(plainToken = "b7aa8e2bb5093a200803a7844d2140ff2f605585", orgCode = "myquant")
        log.info(rsp.toJsonStr())
    }
}