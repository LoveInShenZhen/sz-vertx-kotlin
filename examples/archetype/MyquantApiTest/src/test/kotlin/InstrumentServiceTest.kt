import commons.toLocalDate
import myquant.proto.platform.data.ds_instrument.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.log
import kotlin.time.measureTime

//
// Created by drago on 2023/9/12 012.
//

// api 接口文档: https://gnuixbiqmy.feishu.cn/docs/doccnom7tXsFsFeYatDxoknFYLc#joYCF2

@DisplayName("新版本码表服务接口单元测试")
class InstrumentServiceTest : DsProxyTesterBase() {

    @Test
    @DisplayName("根据 sectype1 = 1010 查询所有股票的码表数据")
    fun TestGetSymbols_by_sectype1() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
            }

            val rsp = instrument_api.getSymbols(req)
            logger.info("根据 sectype1=${req.secType1} 查询码表数据, 共 ${rsp.symbolsCount} 条")
            assert(rsp.symbolsCount > 0)

            val exchanges = mutableSetOf<String>()
            rsp.symbolsList.forEach {
                assert(it.info.secType1 == 1010L)
                exchanges.add(it.info.exchange)
            }

            assert(exchanges.size == 2)
            assert("SHSE" in exchanges)
            assert("SZSE" in exchanges)
        }
    }

    @Test
    @DisplayName("查询SZSE的所有股票的码表数据")
    fun TestGetSymbols_by_sectype1_exchange() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010

                addExchanges("SZSE")
            }

            val rsp = instrument_api.getSymbols(req)
            logger.info("根据 sectype1=${req.secType1}, exchange = ${req.exchangesList.joinToString(",")} 查询码表数据, 共 ${rsp.symbolsCount} 条")
            assert(rsp.symbolsCount > 0)

            rsp.symbolsList.forEach {
                assert(it.info.secType1 == 1010L)
                assert(it.info.exchange == "SZSE")
            }
        }
    }

    @Test
    @DisplayName("根据 symbol 查询码表数据")
    fun TestGetSymbols_by_symbol() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 1)
            assert(rsp.symbolsList.first().info.symbol == "SHSE.600000")
            assert(rsp.symbolsList.first().info.secType1 == 1010L)

            val today = LocalDate.now()
            assert(rsp.symbolsList.first().tradeDate.toLocalDate() == today)
            val ins = rsp.symbolsList.first()
            logger.info("${ins.info.symbol} ${ins.info.secName} ${ins.tradeDate}")
        }
    }

    @Test
    @DisplayName("根据 symbol 查询码表数据, 但是指定错误的 sec_type1")
    fun TestGetSymbols_by_symbol_wrong_sec_type1() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1020
                addSymbols("SHSE.600000")
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("根据 symbol 和 trade_date 查询码表数据")
    fun TestGetSymbols_by_symbol_tradedate() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")

                tradeDate = "2023-09-08"
            }

            val rsp = instrument_api.getSymbols(req)
            logger.info(rsp.symbolsCount.toString())
        }

    }

    @Test
    @DisplayName("根据symbol 在 非交易日 查询码表数据")
    fun TestGetSymbols_by_symbol_not_tradedate() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")

                // 2023-09-10 是周日, 周五 2023-09-08 是交易日
                tradeDate = "2023-09-10"
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("根据symbol 在 1980-01-01(在交易日历起始日期之前) 查询码表数据")
    fun TestGetSymbols_at_1980_01_01() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")

                // 在交易日历起始日期之前
                tradeDate = "1980-01-01"
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("根据symbol 在 2099-01-01(在交易日历截止日期之后) 查询码表数据")
    fun TestGetSymbols_at_2099_01_01() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")

                // 在交易日历截止日期之后
                tradeDate = "2099-01-01"
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("指定今天之后的日期查询码表数据")
    fun TestGetSymbols_after_today() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")

                // 设置为明天
                tradeDate = LocalDate.now().plusDays(1).toString()
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 0)
        }
    }


    @Test
    @DisplayName("一次查询2个symbol的码表数据")
    fun TestGetSymbols_multiple_symbols() {
        MeasureTime {
            val req = GetSymbolsReq {
                secType1 = 1010
                addSymbols("SHSE.600000")
                addSymbols("SZSE.000002")

            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 2)
            rsp.symbolsList.forEach {
                logger.info("instrument symbol: ${it.info.symbol} ${it.info.secName} trade_date: ${it.tradeDate.toLocalDate()} 上市日期 ${it.info.listedDate.toLocalDate()} 退市日期 ${it.info.delistedDate.toLocalDate()}")
            }
        }
    }

    @Test
    @DisplayName("测试查询结果里的 sec_level ")
    fun TestGetSymbol_sec_level() {
        MeasureTime {
            val req = GetSymbolsReq {
                addSymbols("SHSE.515750")
                addSymbols("SZSE.301031")
                addSymbols("SHSE.600253")
            }

            val rsp = instrument_api.getSymbols(req)
            assert(rsp.symbolsCount == 3)
            rsp.symbolsList.forEach {
                logger.info("instrument symbol: ${it.info.symbol} ${it.info.secName} trade_date: ${it.tradeDate.toLocalDate()} sec_level: ${it.secLevel} 上市日期 ${it.info.listedDate.toLocalDate()} 退市日期 ${it.info.delistedDate.toLocalDate()}")
            }
        }
    }

    @Test
    @DisplayName("查询期货连续合约的历史码表")
    fun GetHistorySymbol_ccsymbol() {
        MeasureTime {
            val req = GetHistorySymbolReq {
                symbol = "SHFE.RB"
                startDate = "1991-01-01"
                endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            }

            val rsp = instrument_api.getHistorySymbol(req)
//            rsp.symbolsList.forEach {
//                logger.info("instrument symbol: ${it.info.symbol} ${it.info.secName} trade_date: ${it.tradeDate.toLocalDate()} sec_level: ${it.secLevel} 上市日期 ${it.info.listedDate.toLocalDate()} 退市日期 ${it.info.delistedDate.toLocalDate()}")
//            }
            logger.info("返回记录数: ${rsp.symbolsList.size}")
            val first = rsp.symbolsList.first()
            logger.info("${first.info.secName} 第一条记录日期: ${first.tradeDate.toLocalDate()}\n${first}")
            val last = rsp.symbolsList.last()
            logger.info("${first.info.secName} 最后一条记录日期: ${last.tradeDate.toLocalDate()}\n${last}")
        }
    }

    @Test
    @DisplayName("查询 SHSE.600000 2022 年度的历史码表数据")
    fun TestGetHistorySymbol() {
        MeasureTime {
            val symbol = "SHSE.000010"
            val req = GetHistorySymbolReq {
                this.symbol = symbol
                startDate = "2021-01-01"
                endDate = "2021-12-31"
            }

            val rsp = instrument_api.getHistorySymbol(req)
            assert(rsp.symbolsCount > 0)

            rsp.symbolsList.forEach {
                assert(it.info.symbol == symbol)
            }

            val ins = rsp.symbolsList.first()
            val info = ins.info

            logger.info("${info.symbol} ${info.secName} 在 ${ins.tradeDate.toLocalDate().year} 年度的历史码表数据共 ${rsp.symbolsCount} 条, 从 ${rsp.symbolsList.first().tradeDate.toLocalDate()} ~ ${rsp.symbolsList.last().tradeDate.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("查询 SHSE.600000 2099 年度的历史码表数据")
    fun TestGetHistorySymbol_at_2099() {
        MeasureTime {
            val req = GetHistorySymbolReq {
                symbol = "SHSE.600000"
                startDate = "2099-01-01"
                endDate = "2099-12-31"
            }

            val rsp = instrument_api.getHistorySymbol(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("查询 SHSE.600000 1980 年度的历史码表数据")
    fun TestGetHistorySymbol_at_1980() {
        MeasureTime {
            val req = GetHistorySymbolReq {
                symbol = "SHSE.600000"
                startDate = "1980-01-01"
                endDate = "1980-12-31"
            }

            val rsp = instrument_api.getHistorySymbol(req)
            assert(rsp.symbolsCount == 0)
        }
    }

    @Test
    @DisplayName("不指定日期参数查询 SHSE.600000 历史码表数据")
    fun TestGetHistorySymbol_just_by_symbol() {
        MeasureTime {
            val req = GetHistorySymbolReq {
                symbol = "SHSE.600000"
            }

            val rsp = instrument_api.getHistorySymbol(req)
            assert(rsp.symbolsCount == 1)
            val ins = rsp.symbolsList.first()
            assert(ins.info.symbol == req.symbol)
            logger.info("${ins.info.symbol} ${ins.info.secName} ${ins.tradeDate.toLocalDate()}")
        }
    }

//    @Test
//    @DisplayName("查询今年和明年的历史码表")
//    fun TestGetHistorySymbol_this_year_and_next_year() {
//        val this_year = LocalDate.now().year
//        val next_year = this_year + 1
//
//        val req1 = GetHistorySymbolReq {
//            symbol = "SHSE.600000"
//            startDate = "${this_year}-01-01"
//            endDate = "${this_year}-12-31"
//        }
//
//        val rsp1 = instrument_api.getHistorySymbol(req1)
//
//        val req2 = GetHistorySymbolReq {
//            symbol = "SHSE.600000"
//            startDate = "${this_year}-01-01"
//            endDate = "${next_year}-12-31"
//        }
//
//        val rsp2 = instrument_api.getHistorySymbol(req2)
//
//        assert(rsp1.symbolsCount > 0)
//        assert(rsp2.symbolsCount > 0)
//        assert(rsp1.symbolsCount == rsp2.symbolsCount)
//        assert(rsp1.symbolsList.first().tradeDate == rsp2.symbolsList.first().tradeDate)
//        assert(rsp1.symbolsList.last().tradeDate == rsp2.symbolsList.last().tradeDate)
//
//        val middleIdx = rsp1.symbolsCount / 2
//        assert(rsp1.symbolsList[middleIdx].tradeDate == rsp2.symbolsList[middleIdx].tradeDate)
//
//        val ins = rsp1.symbolsList[middleIdx]
//        logger.info("共 ${rsp1.symbolsCount} 条记录, 中间记录: ${ins.info.symbol} ${ins.info.secName} ${ins.tradeDate.toLocalDate()}")
//    }

    @Test
    @DisplayName("查询1980年的交易日历(不在交易日历有效范围)")
    fun TestGetTradingDatesByYear_1980() {
        MeasureTime {
            val req = GetTradingDatesByYearReq {
                startYear = 1980
                endYear = 1980
            }

            val rsp = instrument_api.getTradingDatesByYear(req)
            assert(rsp.datesCount == 0)
        }
    }

    @Test
    @DisplayName("查询 1980 ~ 1990 年的交易日历")
    fun TestGetTradingDateByYear_1980_1991() {
        MeasureTime {
            val req = GetTradingDatesByYearReq {
                startYear = 1980
                endYear = 1991
            }

            val rsp = instrument_api.getTradingDatesByYear(req)
            assert(rsp.datesCount > 0)
            logger.info("查询结果的日期范围 ${rsp.datesList.first().date.toLocalDate()} ~ ${rsp.datesList.last().date.toLocalDate()}")

            // 交易日历的起始日期 1990-12-19
            assert(rsp.datesList.first().date.toLocalDate().year == 1990)
            assert(rsp.datesList.last().date.toLocalDate().year == req.endYear.toInt())
        }
    }

    @Test
    @DisplayName("查询今年到后2年的交易日历")
    fun TestGetTradingDateByYear_this_year_next_2_year() {
        MeasureTime {
            val this_year = LocalDate.now().year
            val next_2_year = this_year + 2

            val req = GetTradingDatesByYearReq {
                startYear = this_year.toLong()
                endYear = next_2_year.toLong()
            }

            val rsp = instrument_api.getTradingDatesByYear(req)
            logger.info("返回记录数 ${rsp.datesCount}")
            assert(rsp.datesCount > 0)
            rsp.datesList.forEach {
                logger.info("${it.date.toLocalDate()}  ${if (it.hasTradeDate()) "是  " else "不是"} 交易日")
            }
            logger.info("查询结果的日期范围 ${rsp.datesList.first().date.toLocalDate()} ~ ${rsp.datesList.last().date.toLocalDate()}")

            assert(rsp.datesList.first().date.toLocalDate().year == this_year)
        }
    }

    @Test
    @DisplayName("交易日历范围内, 指定交易日的前1个交易日")
    fun TestGetTradingDatesPrevN_1() {
        MeasureTime {
            // 2023-09-08 为周五, 当天是交易日
            val req = GetTradingDatesPrevNReq {
                date = "2023-09-08"
                n = 1
            }

            val rsp = instrument_api.getTradingDatesPrevN(req)
            assert(rsp.tradingDatesCount == req.n.toInt())
            logger.info(
                "${req.date} 的前 ${req.n} 个交易日: ${
                    rsp.tradingDatesList.map { it.toLocalDate().toString() }.joinToString(", ")
                }"
            )
        }

    }

    @Test
    @DisplayName("交易日历范围内, 指定交易日的前10个交易日")
    fun TestGetTradingDatesPrevN_10() {
        MeasureTime {
            // 2023-09-08 为周五, 当天是交易日
            val req = GetTradingDatesPrevNReq {
                date = "2023-09-08"
                n = 10
            }

            val rsp = instrument_api.getTradingDatesPrevN(req)
            assert(rsp.tradingDatesCount == req.n.toInt())

            rsp.tradingDatesList.forEach {
                logger.info("${it.toLocalDate()} ${it.toLocalDate().dayOfWeek}")
            }
        }

    }

    @Test
    @DisplayName("交易日历范围内, 2023-09-10 周日那天的前10个交易日")
    fun TestGetTradingDatesPrevN_10_at_SUNDAY() {
        // 2023-09-10 周日, 当天不是交易日
        val req = GetTradingDatesPrevNReq {
            date = "2023-09-10"
            n = 10
        }

        val rsp = instrument_api.getTradingDatesPrevN(req)
        assert(rsp.tradingDatesCount == req.n.toInt())
        logger.info(
            "${req.date} 的前 ${req.n} 个交易日: ${
                rsp.tradingDatesList.map { "${it.toLocalDate()}[${it.toLocalDate().dayOfWeek}]" }.joinToString(", ")
            }"
        )
    }

    @Test
    @DisplayName("指定日期在交易日历范围之外时, 查询前N个交易日")
    fun TestGetTradingDatesPrevN_outof_range() {
        val req1 = GetTradingDatesPrevNReq {
            date = "2029-01-01"
            n = 10
        }
        val rsp1 = instrument_api.getTradingDatesPrevN(req1)
        assert(rsp1.tradingDatesCount == 0)

        val req2 = GetTradingDatesPrevNReq {
            date = "1980-01-01"
            n = 10
        }
        val rsp2 = instrument_api.getTradingDatesPrevN(req2)
        assert(rsp2.tradingDatesCount == 0)


    }

    @Test
    @DisplayName("指定品种大类, 查询码表基本面")
    fun TestGetSymbolInfos_by_sec_type1() {
        MeasureTime {
            val req = GetSymbolInfosReq {
                secType1 = 1040
            }

            val rsp = instrument_api.getSymbolInfos(req)
            assert(rsp.symbolInfosCount > 0)
            logger.info("共有 ${rsp.symbolInfosCount} 支期货的码表基本面信息")
        }

    }

    @Test
    @DisplayName("debug")
    fun TestGetSymbolInfos_CZCE_AP99() {
        MeasureTime {
            val req = GetSymbolInfosReq {
                secType1 = 1060
                addSymbols("CZCE.AP99")
            }

            val rsp = instrument_api.getSymbolInfos(req)
            logger.info("结果记录 ${rsp.symbolInfosCount} 条记录")
            rsp.symbolInfosList.forEach {
                logger.info("${it.secName} ${it.listedDate.toLocalDate()}")
            }
        }
    }


}