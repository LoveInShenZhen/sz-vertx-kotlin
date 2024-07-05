import com.google.protobuf.Empty
import commons.toLocalDate
import commons.toLocalDateTime
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import myquant.proto.platform.data.DataProto
import myquant.proto.platform.data.data_dists.BatchQueryReq
import myquant.proto.platform.data.data_dists.DataInnerServiceProto
import myquant.proto.platform.data.data_dists.ExchangeSymbols
import myquant.proto.platform.data.ds_instrument.GetSymbolInfosReq
import myquant.proto.platform.data.fundamental.GetPreviousTradingDateReq
import myquant.proto.platform.data.history.GetCurrentTicksReq
import myquant.proto.platform.data.history.GetHistoryBarsNReq
import myquant.proto.platform.data.history.GetHistoryBarsReq
import myquant.proto.platform.data.history.GetHistoryTicksReq
import myquant.rpc.client.TokenUpdater
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.measureTime

//
// Created by drago on 2023/9/22 022.
//


@DisplayName("历史行情服务单元测试")
class DsHistoryTest : DsProxyTesterBase() {

    @Test
    @DisplayName("测试查询日内tick数据的性能, 查询1分钟的tick数据")
    fun TestHistoryTick_intraday_performance() {
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
    fun TestHistoryBarN_60s() {
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
    @DisplayName("查询往年的tick数据")
    fun TestHistoryTick_per_year() {
        measureTime {
            val req = GetHistoryTicksReq {
                symbols = "SHSE.000009 "
                startTime = "2023-09-22 09:40:00"
                endTime = "2023-09-22 09:45:00"
            }

            val rsp = history_api.getHistoryTicks(req)
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            logger.info("第1条:   ${rsp.dataList.first().createdAt.toLocalDateTime()}  ${json_formatter.printToString(rsp.dataList.first())}")
            logger.info("最后1条: ${rsp.dataList.last().createdAt.toLocalDateTime()} ${json_formatter.printToString(rsp.dataList.last())}")
        }

    }

    @Test
    @DisplayName("查询最新tick")
    fun TestCurrentTick() {
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
    fun TestGetHistoryBars_60s() {
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
    fun TestGetHistoryTicks_startTime_after_now() {
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
    fun TestGetHistoryBars_startTime_after_now() {
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
    fun TestGetHistoryTicks_stock_today() {

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
    fun TestGetHistoryTicks_stock_today_2() {
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
    fun TestGetHistoryTicks_stock_yesterday_and_today() {
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
    fun TestGetHistoryTicks_stock_yesterday() {
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
    fun TestGetHistoryBar_60s_stock_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryBarsReq {
            frequency = "60s"
            symbols = "SHSE.600895"
            startTime = "${today} 09:35:00"
            endTime = "${today} 09:40:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
    }

    @Test
    @DisplayName("股票5分钟Bar日内数据查")
    fun TestGetHistoryBar_5m_stock_today() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = GetHistoryBarsReq {
            frequency = "5m"
            symbols = "SHSE.600895"
            startTime = "${today} 09:35:00"
            endTime = "${today} 09:40:00"
        }

        val rsp = history_api.getHistoryBars(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 bar 时间: ${rsp.dataList.first().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 bar 时间: ${rsp.dataList.last().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    @DisplayName("日内5分钟Bar,向上游MongoDb里查询")
    fun UpstreamDataService_BarsBatchQuery() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val req = BatchQueryReq {
            frequency = "5m"
            startTime = "${today} 09:35:00"
            endTime = "${today} 09:40:00"

            addExchangeSymbols(ExchangeSymbols {
                exchange = "SHSE"
                addSymbols("SHSE.600895")
            })
        }

        val rsp = innder_api.barsBatchQuery(req)
        logger.info("查询结果 ${rsp.dataCount} 条记录")
        logger.info("第一条 bar 时间: ${rsp.dataList.first().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        logger.info("最后一条 bar 时间: ${rsp.dataList.last().eob.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    @DisplayName("股票5分钟Barr日前和日内数据查")
    fun TestGetHistoryBar_5m_stock_today_and_yesterday() {
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
    fun TestGetHistoryBar_60s_stock_today_and_yesterday() {
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
    fun TestGetHistoryBar_300s_stock_today() {
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

    @Test
    @DisplayName("当前年度股票日线")
    fun TestGetHistoryBar_1d_stock_this_year() {
        MeasureTime {
            val first_day = "${LocalDate.now().year}-01-01"
            val last_day = "${LocalDate.now().year}-12-31"

            val req = GetHistoryBarsReq {
                frequency = "1d"
                symbols = "SHSE.600000,SZSE.000001"
                startTime = first_day
                endTime = last_day
            }

            val rsp = history_api.getHistoryBars(req)
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            rsp.dataList.forEach {
                logger.info("${it.symbol}  ${it.eob.toLocalDate()}")
            }
        }
    }

    @Test
    @DisplayName("最近2年的股票日线")
    fun TestGetHistoryBar_1d_stock_last_2_year() {
        var rsp: DataProto.Bars? = null
        MeasureTime {
            val first_day = "${LocalDate.now().year-1}-01-01"
            val last_day = "${LocalDate.now().year}-12-31"

            val req = GetHistoryBarsReq {
                frequency = "1d"
                symbols = "SHSE.600000,SZSE.000001"
                startTime = first_day
                endTime = last_day
            }

            rsp = history_api.getHistoryBars(req)
        }

        logger.info("查询结果 ${rsp!!.dataCount} 条记录")
        rsp!!.dataList.take(2).forEach {
            logger.info("${it.symbol} ${it.eob.toLocalDate()}")
        }
        logger.info("...")
        rsp!!.dataList.takeLast(2).forEach {
            logger.info("${it.symbol} ${it.eob.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("DCE.j2309")
    fun TestGetHistoryBar_dce() {
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = "DCE.j2309"
                startTime = "2023-07-01"
                endTime = "2023-07-20"
                frequency = "1d"
            }

            val rsp = history_api.getHistoryBars(req)
            logger.info("查询结果 ${rsp!!.dataCount} 条记录")
            rsp!!.dataList.forEach {
                logger.info("${it.symbol} ${it.eob.toLocalDate()} open: ${it.open} close: ${it.close} volume: ${it.volume}")
            }
        }
    }

    @Test
    @DisplayName("查询虚拟合约日线")
    fun TestGetHistoryBar_1d_ccsymbol() {
        MeasureTime {
            // history(symbol='SHFE.RB', frequency='1d', start_time='2022-09-14 00:00:00', end_time='2023-04-28 00:00:00', adjust=0, adjust_end_time=date, df=True)
            val req = GetHistoryBarsReq {
                symbols = "SHFE.RB"
                frequency = "1d"
                startTime = "2020-09-14 00:00:00"
                endTime = "2023-04-28 00:00:00"
            }

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            logger.info("第一条结果:\n${rsp.dataList.first()}")
            logger.info("最后一条结果:\n${rsp.dataList.last()}")
        }
    }

    @Test
    @DisplayName("日线查询, 过滤掉成交量为0的记录(停牌)")
    fun TestGetHistoryBars_filter_volume_zero() {
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = "SZSE.128127"
                frequency = "1d"
                startTime = "2021-12-14"
                endTime = "2021-12-24"
            }

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            rsp.dataList.forEach {
                logger.info("${it.bob.toLocalDate()} open: ${it.open} close: ${it.close} volume: ${it.volume}")
            }
        }
    }

    @Test
    fun TestGetHistoryBar_sunsen() {
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = "SHSE.600000"
                frequency = "60s"
                startTime = "2022-06-01 11:00:00"
                endTime = "2022-06-01 13:21:00"
            }

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            rsp.dataList.forEach {
                logger.info("${it.bob.toLocalDate()} open: ${it.open} close: ${it.close} volume: ${it.volume} BOB: ${it.bob.toLocalDateTime()} EOB: ${it.eob.toLocalDateTime()}")
            }
        }
    }

    @Test
    @DisplayName("debug")
    fun TestGetHistoryBarsReq_adj() {
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = "SZSE.000001"
                frequency = "1d"
                startTime = "1990-04-21 09:00"
                endTime = "1991-04-29 10:08"
                adjust = 1
                adjustEndTime = "1991-04-29"
            }

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
            rsp.dataList.forEach {
                logger.info("${it.bob.toLocalDate()} open: ${it.open} close: ${it.close} volume: ${it.volume} BOB: ${it.bob.toLocalDateTime()} EOB: ${it.eob.toLocalDateTime()}")
            }
        }
    }

    @Test
    @DisplayName("3000 个symbol 查询1年的日线")
    fun TestGetDayBar_3000_symbols_1year() {
        val req_infos = GetSymbolInfosReq {
            secType1 = 1010
            addExchanges("SHSE")
            addExchanges("SZSE")
        }
        val rsp_infos = instrument_api.getSymbolInfos(req_infos)
        val symbol_list = rsp_infos.symbolInfosList.map { it.symbol }.take(3000)

        val sql = "select count(*) from dists_day_bar where symbol in (${symbol_list.map { "'"+it + "'" }.joinToString(",")}) AND volume > 0 AND trade_date between '2023-01-01' AND '2023-12-31'"
        logger.info(sql)

        logger.info("symbol 数量: ${symbol_list.size}")
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = symbol_list.joinToString(",")
                frequency = "1d"
                startTime = "2023-01-01"
                endTime = "2023-12-31"
                adjust = 1
                adjustEndTime = "2023-12-31"
            }

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
        }
    }

    @Test
    @DisplayName("3000 个symbol 查询1天的日线")
    fun TestGetDayBar_3000_symbols_1day() {
        val req_infos = GetSymbolInfosReq {
            secType1 = 1010
            addExchanges("SHSE")
            addExchanges("SZSE")
        }
        val rsp_infos = instrument_api.getSymbolInfos(req_infos)
        val symbol_list = rsp_infos.symbolInfosList.map { it.symbol }.take(3000)

        logger.info("symbol 数量: ${symbol_list.size}")
        MeasureTime {
            val req = GetHistoryBarsReq {
                symbols = symbol_list.joinToString(",")
                frequency = "1d"
                startTime = "2023-02-15"
                endTime = "2023-02-15"
                adjust = 0
                adjustEndTime = "2023-12-31"
            }

            val sql = "select count(*) from dists_day_bar where symbol in (${symbol_list.map { "'"+it + "'" }.joinToString(",")}) AND volume > 0 AND trade_date between '${req.startTime}' AND '${req.endTime}'"
            logger.info(sql)

            val rsp = history_api.getHistoryBars(req)!!
            logger.info("查询结果 ${rsp.dataCount} 条记录")
        }
    }

    @Test
    fun TestGetHistoryTick_for_revision() {
        MeasureTime {
            val req = GetHistoryTicksReq {
                symbols = "SHSE.600000"
                startTime = "2024-01-18 09:30:00"
                endTime = "2024-01-18 09:31:00"
            }

            val rsp = history_api.getHistoryTicks(req)
            logger.info("查询结果 ${rsp.dataCount} 条记录")

            logger.info("第一条记录:\n${json_formatter.printToString(rsp.dataList.first())}")
            logger.info("最后一条记录:\n${json_formatter.printToString(rsp.dataList.last())}")
        }
    }

    @Test
    fun TestGetHistoryBar_dayBar_n() {
        MeasureTime {
            val req = GetHistoryBarsNReq {
                symbol = "SHSE.601318"
                frequency = "1d"
                endTime = "2024-03-23 00:00:00"
                count = 150
                adjust = 1
            }

            val rsp = history_api.getHistoryBarsN(req)
            TokenUpdater.logger.info("查询结果 ${rsp.dataCount} 条记录")


            TokenUpdater.logger.info("第一条记录:\n${json_formatter.printToString(rsp.dataList.first())}")
            TokenUpdater.logger.info("最后一条记录:\n${json_formatter.printToString(rsp.dataList.last())}")
        }
    }

    @Test
    @DisplayName("批量测试日线-n查询性能")
    fun TestGetHistoryBar_dayBar_n_bat() {
        // 先查询沪市所有股票代码
        val req_symbols = GetSymbolInfosReq {
            secType1 = 1010
            addExchanges("SZSE")
        }

        val rsp_symbols = instrument_api.getSymbolInfos(req_symbols)

        logger.info("测试 ${rsp_symbols.symbolInfosList.count()} 支股票的日线-n查询")

        val time_consuming_list = mutableListOf<Long>()

        rsp_symbols.symbolInfosList.forEach { symbolInfo ->
            val duration = measureTime {
                val req = GetHistoryBarsNReq {
                    symbol = symbolInfo.symbol
                    frequency = "1d"
                    endTime = "2024-03-23 00:00:00"
                    count = 150
                    adjust = 1
                }

                val rsp = history_api.getHistoryBarsN(req)
                logger.info("${req.symbol} 查询结果 ${rsp.dataCount} 条记录")
            }
            time_consuming_list.add(duration.toLong(DurationUnit.MILLISECONDS))
        }

        val df = time_consuming_list.toDataFrame()
//        val desc = df.describe()
        logger.info("count:     ${df.count()}")
        logger.info("max:       ${df.max()}")
        logger.info("min:       ${df.min()}")
        logger.info("mean:      ${df.mean()}")
        logger.info("median:    ${df.median()}")
    }

    @Test
    @DisplayName("测试bar合成")
    fun TestHistoryBar_1m() {
        measureTime {
            val req = GetHistoryBarsReq {
                symbols = "SZSE.123167"
                frequency = "3600s"
//                frequency = "60s"
                startTime = "2024-03-25 08:00:00"
                endTime = "2024-03-25 15:00:00"
            }

            val rsp = history_api.getHistoryBars(req)
            rsp.dataList.forEach {
                logger.info("${json_formatter.printToString(it)}")
                logger.info("close: ${it.close}, BOB: ${it.bob.toLocalDateTime()} EOB: ${it.eob.toLocalDateTime()}")
            }
        }
    }

    @Test
    fun debug_test() {
        val req = GetHistoryBarsNReq {
            symbol = "SZSE.159955"
            frequency = "1d"
            count = 10
            endTime = "2020-01-06"
            adjust = 1
            adjustEndTime = "2024-04-29"
        }

        val rsp = history_api.getHistoryBarsN(req)
        logger.info("结果条数: ${rsp.dataCount}")

        rsp.dataList.forEach{
            logger.info("${it.eob.toLocalDate()} ${json_formatter.printToString(it)}")
        }
    }
}