import commons.toLocalDate
import myquant.proto.platform.data.fundamental.*
import myquant.proto.platform.data.fundamental.FundamentalServiceProto.GetDividendsReq
import myquant.proto.platform.data.fundamental.FundamentalServiceProto.GetHistoryInstrumentsReq
import myquant.proto.platform.data.fundamental.FundamentalServiceProto.GetInstrumentInfosReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.math.log

//
// Created by drago on 2023/9/12 012.
//

@DisplayName("老接口-码表查询接口测试")
class FundamentalTest : DsProxyTesterBase() {

    @Test
    @DisplayName("默认参数下查询全市场码表最新数据")
    fun GetInstrumentInfos_test() {
        MeasureTime {
            val req = GetInstrumentInfosReq {}

            val rsp = fundamental_api.getInstrumentInfos(req)
            logger.info("getInstrumentInfos count: ${rsp.dataCount}")
            // 无异常, 有数据
            assert(rsp.dataCount > 0)
        }

    }

    @Test
    @DisplayName("查询指定 symbol 的基本面")
    fun GetInstrumentInfos_by_symbols() {
        MeasureTime {
            val req = GetInstrumentInfosReq {
                symbols = "SHSE.600000,GFEX.SI"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            assert(rsp.dataCount == 2)
            val symbols = req.symbols.split(",").toSet()
            rsp.dataList.forEach {
                assert(it.symbol in symbols)
            }
        }
    }

    @Test
    @DisplayName("查询指定交易所所的基本面数据")
    fun GetInstrumentInfos_by_exchange() {
        MeasureTime {
            val req = GetInstrumentInfosReq {
                exchanges = "SHFE,GFEX"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            assert(rsp.dataCount > 0)   // 有数据
            val exchanges = req.exchanges.split(",").toSet()
            rsp.dataList.forEach {
                assert(it.exchange in exchanges)
            }
            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }
    }

    @Test
    @DisplayName("根据sec_type查询基本面")
    fun GetInstrumentInfos_by_sec_type() {
        MeasureTime {
            val req = GetInstrumentInfosReq {
                secTypes = "10"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            assert(rsp.dataCount > 0)   // 有数据
            rsp.dataList.forEach {
                assert(it.secType == 10)
            }

            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }

    }

    @Test
    @DisplayName("根据交易所和sec_type查询基本面")
    fun GetInstrumentInfos_by_exchange_and_sec_type() {
        MeasureTime {
            val req = GetInstrumentInfosReq {
                exchanges = "GFEX"
                secTypes = "10"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            assert(rsp.dataCount > 0)   // 有数据
            rsp.dataList.forEach {
                assert(it.secType == 10)
                assert(it.exchange == "GFEX")
            }

            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }
    }

    @Test
    @DisplayName("期货交易所里查询股票的基本面")
    fun GetInstrumentInfos_stock_in_fut_exchange() {
        MeasureTime {
            val req = GetInstrumentInfosReq {
                exchanges = "GFEX"
                secTypes = "0"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            assert(rsp.dataCount == 0)   // 没有数据
        }
    }

    @Test
    @DisplayName("查询指定symbol的码表数据")
    fun GetInstruments_by_symbols() {
        MeasureTime {
            val req = GetInstrumentsReq{
                symbols = "SHSE.600000,GFEX.SI"
            }

            val rsp = fundamental_api.getInstruments(req)
            assert(rsp.dataCount == 2)

            val symbols = req.symbols.split(",").toSet()
            rsp.dataList.forEach {
                assert(it.symbol in symbols)
            }
            rsp.dataList.forEach{
                logger.info("${it.symbol}    ${it.info.secName}")
            }

            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }
    }

    @Test
    @DisplayName("查询指定交易所的码表数据")
    fun GetInstruments_by_exchanges() {
        MeasureTime {
            val req = GetInstrumentsReq {
                exchanges = "SHFE,GFEX"
            }

            val rsp = fundamental_api.getInstruments(req)
            assert(rsp.dataCount > 0)

            val exchanges = req.exchanges.split(",").toSet()
            rsp.dataList.forEach {
                assert(it.info.exchange in exchanges)
            }
        }
    }

    @Test
    @DisplayName("根据 sec_type 查询码表数据- 连续合约")
    fun GetInstruments_by_sec_type() {
        MeasureTime {
            val req = GetInstrumentsReq {
                secTypes = "10"
            }

            val rsp = fundamental_api.getInstruments(req)
            assert(rsp.dataCount > 0)

            rsp.dataList.forEach {
                assert(it.info.secType == 10)
            }

            rsp.dataList.take(10).forEach {
                logger.info("${it.symbol}  ${it.info.secName}")
            }

            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }
    }

    @Test
    @DisplayName("查询股票/基金/债券, 略过ST股票")
    fun GetInstruments_skip_st() {
        MeasureTime {
            val req = GetInstrumentsReq {
                exchanges = "SHSE,SZSE"
                skipSt = true
            }

            val rsp = fundamental_api.getInstruments(req)
            assert(rsp.dataCount > 0)

            val today = LocalDate.now()

            rsp.dataList.forEach {
                if (today.isBefore(it.info.delistedDate.toLocalDate()) ) {
                    // 在市股票
                    assert(it.info.secName.contains("ST").not())
                }
            }

            rsp.dataList.take(10).forEach {
                logger.info("${it.symbol}  ${it.info.secName}")
            }

            logger.info("返回结果记录 ${rsp.dataCount} 条")
        }

    }

    @Test
    @DisplayName("查询股票的分红送配, symbol, 起始日期都是有效值")
    fun GetDividends_stock() {
        MeasureTime {
            val req = GetDividendsReq {
                symbol = "SHSE.601318"
                startDate = "2022-01-01"
                endDate = "2023-12-31"
            }

            val rsp = fundamental_api.getDividends(req)
            assert(rsp.dataCount > 0)
            rsp.dataList.forEach {
                logger.info("${it.symbol} ${it.createdAt.toLocalDate()}")
            }
        }

    }

    @Test
    @DisplayName("查询股票的分红送配, symbol, 起始日期都是有效值")
    fun GetDividends_stock_start_1980() {
        val req = GetDividendsReq {
            symbol = "SHSE.601318"
            startDate = "1980-01-01"
            endDate = "2029-12-31"
        }

        val rsp = fundamental_api.getDividends(req)
        assert(rsp.dataCount > 0)
        logger.info("结果记录 ${rsp.dataCount} 条分红送配记录")
        rsp.dataList.forEach {
            logger.info("${it.symbol} ${it.createdAt.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("查询基金的分红送配, symbol, 起始日期都是有效值")
    fun GetDividends_fund() {
        val req = GetDividendsReq {
            symbol = "SZSE.159919"
            startDate = "1980-01-01"
            endDate = "2029-12-31"
        }

        val rsp = fundamental_api.getDividends(req)
        assert(rsp.dataCount > 0)
        logger.info("结果记录 ${rsp.dataCount} 条分红送配记录")
        rsp.dataList.forEach {
            logger.info("${it.symbol} ${it.createdAt.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("查询全部分红数据")
    fun GetDividends_all() {
        MeasureTime {
            val req = GetDividendsSnapshotReq {
            }

            val rsp = fundamental_api.getDividendsSnapshot(req)
            logger.info("结果记录 ${rsp.dataCount} 条分红送配记录")
        }
    }

    @Test
    @DisplayName("debug")
    fun GetHistoryInstrument_debug_1() {
        MeasureTime {
            val req = GetHistoryInstrumentsReq {
                symbols = "SZSE.000001"
                startDate = "1990-04-21"
                endDate = "1991-04-29"
            }

            val rsp = fundamental_api.getHistoryInstruments(req)
            rsp.dataList.forEach {
                logger.info("${it.symbol}  ${it.createdAt.toLocalDate()}")
            }
        }
    }

    @Test
    @DisplayName("测试码表查询 sec_level")
    fun GetInstrument_sec_level() {
        MeasureTime {
            val req = GetInstrumentsReq {
                symbols = "SHSE.515750,SZSE.301031,SHSE.600253"
            }

            val rsp = fundamental_api.getInstruments(req)
            rsp.dataList.forEach {
                logger.info("instrument symbol: ${it.info.symbol} ${it.info.secName} trade_date: ${it.createdAt.toLocalDate()} sec_level: ${it.secLevel} 上市日期 ${it.info.listedDate.toLocalDate()} 退市日期 ${it.info.delistedDate.toLocalDate()}")
            }
        }
    }

    @Test
    @DisplayName("查询期货码表")
    fun GetInstrumentInfos_fut() {
        MeasureTime {
            val req = GetInstrumentInfosReq{
                symbols = "SHFE.al0311,DCE.m0203,DEC.a0211"
            }

            val rsp = fundamental_api.getInstrumentInfos(req)
            rsp.dataList.forEach {
                logger.info(it.toString())
                logger.info("=".repeat(64))
            }
        }
    }

}