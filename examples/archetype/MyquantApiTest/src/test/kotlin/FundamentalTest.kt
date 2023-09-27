import commons.toLocalDate
import myquant.proto.platform.data.fundamental.GetDividendsReq
import myquant.proto.platform.data.fundamental.GetInstrumentInfosReq
import myquant.proto.platform.data.fundamental.GetInstrumentsReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

//
// Created by drago on 2023/9/12 012.
//

@DisplayName("老接口-码表查询接口测试")
class FundamentalTest : DsProxyTesterBase() {

    @Test
    @DisplayName("默认参数下查询全市场码表最新数据")
    fun GetInstrumentInfos_test() {
        val req = GetInstrumentInfosReq {

        }

        val rsp = fundamental_api.getInstrumentInfos(req)
        logger.info("getInstrumentInfos count: ${rsp.dataCount}")
        // 无异常, 有数据
        assert(rsp.dataCount > 0)
    }

    @Test
    @DisplayName("查询指定 symbol 的基本面")
    fun GetInstrumentInfos_by_symbols() {
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

    @Test
    @DisplayName("查询指定交易所所的基本面数据")
    fun GetInstrumentInfos_by_exchange() {
        val req = GetInstrumentInfosReq {
            exchanges = "SHFE,GFEX"
        }

        val rsp = fundamental_api.getInstrumentInfos(req)
        assert(rsp.dataCount > 0)   // 有数据
        val exchanges = req.exchanges.split(",").toSet()
        rsp.dataList.forEach {
            assert(it.exchange in exchanges)
        }
    }

    @Test
    @DisplayName("根据sec_type查询基本面")
    fun GetInstrumentInfos_by_sec_type() {
        val req = GetInstrumentInfosReq {
            secTypes = "10"
        }

        val rsp = fundamental_api.getInstrumentInfos(req)
        assert(rsp.dataCount > 0)   // 有数据
        rsp.dataList.forEach {
            assert(it.secType == 10)
        }
    }

    @Test
    @DisplayName("根据交易所和sec_type查询基本面")
    fun GetInstrumentInfos_by_exchange_and_sec_type() {
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
    }

    @Test
    @DisplayName("期货交易所里查询股票的基本面")
    fun GetInstrumentInfos_stock_in_fut_exchange() {
        val req = GetInstrumentInfosReq {
            exchanges = "GFEX"
            secTypes = "0"
        }

        val rsp = fundamental_api.getInstrumentInfos(req)
        assert(rsp.dataCount == 0)   // 没有数据
    }

    @Test
    @DisplayName("查询指定symbol的码表数据")
    fun GetInstruments_by_symbols() {
        val req = GetInstrumentsReq{
            symbols = "SHSE.600000,GFEX.SI"
        }

        val rsp = fundamental_api.getInstruments(req)
        assert(rsp.dataCount == 2)

        val symbols = req.symbols.split(",").toSet()
        rsp.dataList.forEach {
            assert(it.symbol in symbols)
        }
    }

    @Test
    @DisplayName("查询指定交易所的码表数据")
    fun GetInstruments_by_exchanges() {
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

    @Test
    @DisplayName("根据 sec_type 查询码表数据")
    fun GetInstruments_by_sec_type() {
        val req = GetInstrumentsReq {
            secTypes = "10"
        }

        val rsp = fundamental_api.getInstruments(req)
        assert(rsp.dataCount > 0)

        rsp.dataList.forEach {
            assert(it.info.secType == 10)
        }
    }

    @Test
    @DisplayName("查询股票/基金/债券, 略过ST股票")
    fun GetInstruments_skip_st() {
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
    }

    @Test
    @DisplayName("查询股票的分红送配, symbol, 起始日期都是有效值")
    fun GetDividends_stock() {
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
            symbol = "SZSE.161911"
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


}