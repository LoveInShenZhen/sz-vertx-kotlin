import io.grpc.Metadata
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.Status
import io.grpc.stub.MetadataUtils
import myquant.proto.platform.data.history.GetCurrentTicksReq
import myquant.proto.platform.data.history.HistoryServiceGrpcKt
import myquant.proto.platform.data.history.HistoryServiceProto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference

//
// Created by drago on 2023/9/20 020.
//

@DisplayName("测试ds-proxy的api流控功能")
class RateLimitTest : DsProxyTesterBase() {

    @Test
    @DisplayName("GetCurrentTicks 流控测试: 批量查询")
    fun GetCurrentTicks_batch() {
        val symbol_txt = """SHSE.000037
SHSE.000038
SHSE.000039
SHSE.000040
SHSE.000041
SHSE.000042
SHSE.000043
SHSE.000044
SHSE.000045
SHSE.000046
SHSE.000047
SHSE.000048
SHSE.000049
SHSE.000050
SHSE.000051
SHSE.000052
SHSE.000053
SHSE.000054
SHSE.000055
SHSE.000056
SHSE.000057
SHSE.000058
SHSE.000059
SHSE.000060
SHSE.000061
SHSE.000062
SHSE.000063
SHSE.000064
SHSE.000065
SHSE.000066
SHSE.000067
SHSE.000068
SHSE.000069
SHSE.000070
"""

        val symbol_list = symbol_txt.split("\n").map { it.trim() }
        val req = GetCurrentTicksReq {
            symbols = symbol_list.joinToString(",")
        }

        val headersCapture = AtomicReference<Metadata>()
        val trailersCapture = AtomicReference<Metadata>()

        val hapi =
            history_api.withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture))

        for (i in 1..10) {
            try {
                val rsp = hapi.getCurrentTicks(req)
                logger.info("返回结果条数: ${rsp.dataCount}")
            } catch (ex: io.grpc.StatusRuntimeException) {
                if (ex.status.code == Status.RESOURCE_EXHAUSTED.code) {
                    logger.warn(ex.message)
                    logger.warn(
                        "Retry-After : ${
                            headersCapture.get().get(Metadata.Key.of("Retry-After", ASCII_STRING_MARSHALLER))
                        }"
                    )
                } else {
                    throw ex
                }
            }

        }
    }

    @Test
    @DisplayName("GetCurrentTicks 流控测试: 批量查询; 请求上下文里设置x-code : 666,999, 豁免流控检查和权限检查")
    fun GetCurrentTicks_batch_disable_rate_limit() {
        val symbol_txt = """SHSE.000037
SHSE.000038
SHSE.000039
SHSE.000040
SHSE.000041
SHSE.000042
SHSE.000043
SHSE.000044
SHSE.000045
SHSE.000046
SHSE.000047
SHSE.000048
SHSE.000049
SHSE.000050
SHSE.000051
SHSE.000052
SHSE.000053
SHSE.000054
SHSE.000055
SHSE.000056
SHSE.000057
SHSE.000058
SHSE.000059
SHSE.000060
SHSE.000061
SHSE.000062
SHSE.000063
SHSE.000064
SHSE.000065
SHSE.000066
SHSE.000067
SHSE.000068
SHSE.000069
SHSE.000070
"""

        val symbol_list = symbol_txt.split("\n").map { it.trim() }
        val req = GetCurrentTicksReq {
            symbols = symbol_list.joinToString(",")
        }

        val headersCapture = AtomicReference<Metadata>()
        val trailersCapture = AtomicReference<Metadata>()

        val x_code_meta = Metadata()
        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "666")
        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "999")
//        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "666,999")

        val hapi = history_api.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(x_code_meta),
            MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture)
        )

        for (i in 1..10) {
            try {
                val rsp = hapi.getCurrentTicks(req)
                logger.info("返回结果条数: ${rsp.dataCount}")
            } catch (ex: io.grpc.StatusRuntimeException) {
                if (ex.status.code == Status.RESOURCE_EXHAUSTED.code) {
                    logger.warn(ex.message)
                    logger.warn(
                        "Retry-After : ${
                            headersCapture.get().get(Metadata.Key.of("Retry-After", ASCII_STRING_MARSHALLER))
                        }"
                    )
                } else {
                    throw ex
                }
            }

        }
    }

    @Test
    @DisplayName("GetCurrentTicks 流控测试: 同一个symbo循环10次变查询")
    fun GetCurrentTicks_repeated() {
        val symbol = "SHSE.600000"
        val req = GetCurrentTicksReq {
            symbols = symbol
        }

        val headersCapture = AtomicReference<Metadata>()
        val trailersCapture = AtomicReference<Metadata>()

        val hapi =
            history_api.withInterceptors(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture))

        for (i in 1..100) {
            try {
                val rsp = hapi.getCurrentTicks(req)
                logger.info("(${i}) ${symbol} 最新价格: ${rsp.dataList.first().price}")
            } catch (ex: io.grpc.StatusRuntimeException) {
                if (ex.status.code == Status.RESOURCE_EXHAUSTED.code) {
                    logger.warn(ex.message)
                    logger.warn(
                        "Retry-After : ${
                            headersCapture.get().get(Metadata.Key.of("Retry-After", ASCII_STRING_MARSHALLER))
                        }"
                    )
                } else {
                    throw ex
                }
            }

        }
    }

    @Test
    @DisplayName("GetCurrentTicks 流控测试: 同一个symbo循环10次变查询, 请求上下文里设置x-code : 666,999, 豁免流控检查和权限检查")
    fun GetCurrentTicks_repeated_disable_rate_limit() {
        val symbol = "SHSE.600000"
        val req = GetCurrentTicksReq {
            symbols = symbol
        }

        val headersCapture = AtomicReference<Metadata>()
        val trailersCapture = AtomicReference<Metadata>()

        val x_code_meta = Metadata()
//        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "666")
//        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "999")
        x_code_meta.put(Metadata.Key.of("X-CODE", ASCII_STRING_MARSHALLER), "666,999")

        val hapi = history_api.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(x_code_meta),
            MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture)
        )

        for (i in 1..10) {
            try {
                val rsp = hapi.getCurrentTicks(req)
                logger.info("(${i}) ${symbol} 最新价格: ${rsp.dataList.first().price}")
            } catch (ex: io.grpc.StatusRuntimeException) {
                if (ex.status.code == Status.RESOURCE_EXHAUSTED.code) {
                    logger.warn(ex.message)
                    logger.warn(
                        "Retry-After : ${
                            headersCapture.get().get(Metadata.Key.of("Retry-After", ASCII_STRING_MARSHALLER))
                        }"
                    )
                } else {
                    throw ex
                }
            }

        }

    }


}