import com.google.protobuf.Empty
import com.google.protobuf.EmptyProto
import commons.toLocalDate
import commons.toLocalDateTime
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import jdk.jfr.Frequency
import myquant.proto.platform.data.fundamental.GetPreviousTradingDateReq
import myquant.proto.platform.data.history.GetCurrentTicksReq
import myquant.proto.platform.data.history.GetHistoryBarsNReq
import myquant.proto.platform.data.history.GetHistoryBarsReq
import myquant.proto.platform.data.history.GetHistoryTicksReq
import myquant.proto.platform.data.history.HistoryServiceProto.GetHistoryTicksReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.log

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
        ).withDeadlineAfter(10, TimeUnit.MINUTES)


        for (i in 1..50) {
            val begin_time = System.currentTimeMillis()
            val rsp = hapi.getHistoryTicks(req)
            val end_time = System.currentTimeMillis()

            val duration = end_time - begin_time
            logger.info("查询结果 ${rsp.dataCount} 条, 耗时 ${duration} 毫秒")
        }
    }

    @Test
    @DisplayName("HistoryBarN_60s")
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

    @Test
    @DisplayName("查询历史分钟线")
    fun GetHistoryBars_60s() {
        val req = GetHistoryBarsReq {
            symbols = "SHSE.600000"
            frequency = "60s"
            startTime = "2023-10-13 09:00:00"
            endTime = "2023-10-20 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("查询tick起始时间在当前时间之后")
    fun GetHistoryTicks_startTime_after_now() {
        val start_time = LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val end_time = LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val req = GetHistoryTicksReq {
            symbols = "SHSE.600000"
            startTime = start_time
            endTime = end_time
        }
        val rsp = history_api.getHistoryTicks(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("查询分钟Bar起始时间在当前时间之后")
    fun GetHistoryBars_startTime_after_now() {
        val start_time = LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val end_time = LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val req = GetHistoryBarsReq {
            symbols = "SHSE.600000"
            frequency = "60s"
            startTime = start_time
            endTime = end_time
        }
        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("股票tick数据查询优化-日内")
    fun GetHistoryTicks_stock_today() {

        val begin_time = System.currentTimeMillis()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryTicksReq {
            symbols = "SHSE.600000"
            startTime = "${today} 09:30:00"
            endTime = "${today} 09:32:00"
        }

        val rsp = history_api.getHistoryTicks(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        val end_time = System.currentTimeMillis()
        val duration = end_time - begin_time
        logger.info("查询结果 ${rsp.dataCount} 条记录, 耗时: ${duration} 毫秒")
    }

    @Test
    @DisplayName("股票一个symbol全天tick")
    fun GetHistoryTicks_stock_today_2() {
        val ping_req = Empty.newBuilder().build()
        health_api.ping(ping_req)

        val begin_time = System.currentTimeMillis()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryTicksReq {
            symbols = "SHSE.600029"
            startTime = "${today} 00:00:00"
            endTime = "${today} 23:59:59"
        }

        val hapi = history_api
        val rsp = hapi.getHistoryTicks(req)
        val end_time = System.currentTimeMillis()
        val duration = end_time - begin_time
        logger.info("查询结果 ${rsp.dataCount} 条记录, 耗时: ${duration} 毫秒")
    }

    @Test
    @DisplayName("股票tick数据查询优化-日前+日内")
    fun GetHistoryTicks_stock_yesterday_and_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val preTradeDay = preTradeDay()

        val req = GetHistoryTicksReq {
            symbols = "SHSE.600000"
            startTime = "${preTradeDay} 14:55:00"
            endTime = "${today} 09:35:00"
        }

        val rsp = history_api.getHistoryTicks(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 tick 时间: ${rsp.dataList.first().createdAt.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 tick 时间: ${rsp.dataList.last().createdAt.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
    }

    fun preTradeDay() : String {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetPreviousTradingDateReq {
            this.date = today
        }

        val rsp = fundamental_api.getPreviousTradingDate(req)
        return rsp.date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @Test
    @DisplayName("股票tick数据查询优化-日前")
    fun GetHistoryTicks_stock_yesterday() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val preTradeDay = preTradeDay()

        val req = GetHistoryTicksReq {
            symbols = "SHSE.600000"
            startTime = "${preTradeDay} 14:50:00"
            endTime = "${preTradeDay} 14:55:00"
        }

        val rsp = history_api.getHistoryTicks(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 tick 时间: ${rsp.dataList.first().createdAt.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 tick 时间: ${rsp.dataList.last().createdAt.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
    }

    @Test
    @DisplayName("股票分钟Bar日内数据查询")
    fun GetHistoryBar_60s_stock_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryBarsReq {
            frequency = "60s"
            symbols = "SHSE.600000"
            startTime = "${today} 09:00:00"
            endTime = "${today} 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("股票5分钟Bar日内数据查")
    fun GetHistoryBar_5m_stock_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryBarsReq {
            frequency = "5m"
            symbols = "SHSE.600000"
            startTime = "${today} 09:00:00"
            endTime = "${today} 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 bar 时间: ${rsp.dataList.first().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 bar 时间: ${rsp.dataList.last().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
    }

    @Test
    @DisplayName("股票5分钟Barr日前和日内数据查")
    fun GetHistoryBar_5m_stock_today_and_yesterday() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val preTradeDay = preTradeDay()

        val req = GetHistoryBarsReq {
            frequency = "5m"
            symbols = "SHSE.600000"
            startTime = "${preTradeDay} 09:00:00"
            endTime = "${today} 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 bar 时间: ${rsp.dataList.first().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 bar 时间: ${rsp.dataList.last().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
    }

    @Test
    @DisplayName("股票分钟Bar日前和日内数据查询")
    fun GetHistoryBar_60s_stock_today_and_yesterday() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val preTradeDay = preTradeDay()

        val req = GetHistoryBarsReq {
            frequency = "60s"
            symbols = "SHSE.600000"
            startTime = "${preTradeDay} 09:00:00"
            endTime = "${today} 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("股票5分钟Bar日内数据查询")
    fun GetHistoryBar_300s_stock_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryBarsReq {
            frequency = "300s"
            symbols = "SHSE.600000"
            startTime = "${today} 09:00:00"
            endTime = "${today} 15:00:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }
}