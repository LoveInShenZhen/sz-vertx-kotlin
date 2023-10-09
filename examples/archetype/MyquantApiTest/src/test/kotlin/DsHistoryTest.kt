import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import myquant.proto.platform.data.history.GetCurrentTicksReq
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

    @Test
    @DisplayName("查询最新tick")
    fun CurrentTick() {
        val symbols="""SHSE.000001
SHSE.000002
SHSE.000003
SHSE.000004
SHSE.000005
SHSE.000006
SHSE.000007
SHSE.000008
SHSE.000009
SHSE.000010
SHSE.000011
SHSE.000012
SHSE.000013
SHSE.000015
SHSE.000016
SHSE.000017
SHSE.000018
SHSE.000019
SHSE.000020
SHSE.000021
SHSE.000022
SHSE.000025
SHSE.000026
SHSE.000027
SHSE.000028
SHSE.000029
SHSE.000030
SHSE.000031
SHSE.000032
SHSE.000033
SHSE.000034
SHSE.000035
SHSE.000036
SHSE.000037
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
"""
        val symbol_list = symbols.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        symbol_list.forEach {symbol ->
            val req = GetCurrentTicksReq {
                this.symbols = symbol
            }
            val begin_time = System.currentTimeMillis()
            val rsp = history_api.getCurrentTicks(req)
            val end_time = System.currentTimeMillis()

            val duration = end_time - begin_time
            if (rsp.dataCount > 0) {
                logger.info("${symbol} 查询最新价格 ${rsp.dataList.first().price}, 耗时 ${duration} 毫秒")
            }

        }
    }
}