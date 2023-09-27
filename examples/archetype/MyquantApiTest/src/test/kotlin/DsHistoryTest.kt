import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import myquant.proto.platform.data.history.GetHistoryBarsNReq
import myquant.proto.platform.data.history.GetHistoryTicksReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//
// Created by drago on 2023/9/22 022.
//


@DisplayName("历史行情服务单元测试")
class DsHistoryTest : DsProxyTesterBase() {

    @Test
    @DisplayName("测试查询日内tick数据的性能, 查询1分钟的tick数据")
    fun HistoryTick_intraday_performance() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryTicksReq {
            symbols = "SHSE.600000"
            startTime = "${today} 09:30:00"
            endTime = "${today} 09:31:00"
        }

        val x_code_meta = Metadata()
        x_code_meta.put(Metadata.Key.of("X-CODE", Metadata.ASCII_STRING_MARSHALLER), "666,999")

        val hapi = history_api.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(x_code_meta)
        )

        for (i in 1..50) {
            val begin_time = System.currentTimeMillis()
            val rsp = hapi.getHistoryTicks(req)
            val end_time = System.currentTimeMillis()

            val duration = end_time - begin_time
            logger.info("查询结果 ${rsp.dataCount} 条, 耗时 ${duration} 毫秒")
        }
    }

    @Test
    @DisplayName("")
    fun HistoryBarN_60s() {
        val req = GetHistoryBarsNReq {
            symbol = "SHSE.600000"
            frequency = "60s"
            endTime = "2023-09-22 17:12:00"
            count = 150
            adjust = 1
        }

        val x_code_meta = Metadata()
        x_code_meta.put(Metadata.Key.of("X-CODE", Metadata.ASCII_STRING_MARSHALLER), "666,999")

        val hapi = history_api.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(x_code_meta)
        )

        val rsp = hapi.getHistoryBarsN(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }
}