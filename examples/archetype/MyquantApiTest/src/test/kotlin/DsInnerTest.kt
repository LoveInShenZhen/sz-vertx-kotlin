import commons.toLocalDateTime
import myquant.proto.platform.data.data_dists.BatchQueryReq
import myquant.proto.platform.data.data_dists.DataInnerServiceProto
import myquant.proto.platform.data.data_dists.DataInnerServiceProto.ExchangeSymbols
import myquant.proto.platform.data.data_dists.DayBarsReq
import myquant.proto.platform.data.data_dists.ExchangeSymbols
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//
// Created by drago on 2023/12/15 015.
//

@DisplayName("数据缓存代理内部数据服务")
class DsInnerTest : DsProxyTesterBase() {

    fun loadSymbols(count: Int): List<String> {
        val symbols_file_path = """C:\Users\drago\work\tmp\myquant_ins_instrument_last.csv"""
        val sfile = File(symbols_file_path)
        return sfile.readLines().take(count)
    }

    fun doTicksBatchQuery_PerformanceTest(count: Int) {
        val symbols = this.loadSymbols(count)

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val req = BatchQueryReq {
            startTime = "$today 09:40:00"
            endTime = "$today 09:41:00"
            addExchangeSymbols(ExchangeSymbols {
                exchange = "SHSE"
                addAllSymbols(symbols)
            })

        }

        val begin_time = System.currentTimeMillis()

        val rpc_api = innder_api.withMaxInboundMessageSize(1024 * 1024 * 64)

        val rsp = rpc_api.ticksBatchQuery(req)

        val end_time = System.currentTimeMillis()

        val duration = end_time - begin_time

        logger.info("日内, ${count} 个symbol 1 分钟内的tick数据查询结果 ${rsp.dataCount} 条记录, 耗时: ${duration} 毫秒")
    }

    @Test
    @DisplayName("日内tick截面查询-性能测试-100_symbols")
    fun TicksBatchQuery_Performance_100() {
        doTicksBatchQuery_PerformanceTest(100)
    }

    @Test
    @DisplayName("日内tick截面查询-性能测试-1000_symbols")
    fun TicksBatchQuery_Performance_1000() {
        doTicksBatchQuery_PerformanceTest(1000)
    }

    @Test
    @DisplayName("日内tick截面查询-性能测试-2000_symbols")
    fun TicksBatchQuery_Performance_2000() {
        doTicksBatchQuery_PerformanceTest(2000)
    }

    @Test
    @DisplayName("日内tick截面查询-性能测试-3000_symbols")
    fun TicksBatchQuery_Performance_3000() {
        doTicksBatchQuery_PerformanceTest(3000)
    }

    @Test
    fun TestDayBars() {
        val req = DayBarsReq {
            symbol = "SZSE.000001"
            fromDay = "1991-04-12"
            toDay = "1991-04-29"
        }

        val rsp = innder_api.dayBars(req)
        rsp.dataList.forEach{
            logger.info("${it.bob.toLocalDateTime()}")
        }
    }
}