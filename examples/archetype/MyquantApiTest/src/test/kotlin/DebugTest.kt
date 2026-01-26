import commons.pbToJsonStr
import commons.toLocalDate
import myquant.proto.platform.data.ds_instrument.InstrumentServiceProto
import myquant.proto.platform.data.history.HistoryServiceProto.GetHistoryBarsReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

//
// Created by drago on 2025/6/16 周一.
//

@DisplayName("")
class DebugTest : DebugTesterBase() {

    @Test
    @DisplayName("")
    fun bug_001_test() {
        // 先获取指定日期的 ETF symbols

        val trade_date = "2023-03-01"
        val get_etf_symbols_req = InstrumentServiceProto.GetSymbolsReq.newBuilder().apply {
            tradeDate = trade_date
            secType1 = 1020
            skipSuspended = true
        }.build()
        val get_etf_symbols_rsp = instrument_api.getSymbols(get_etf_symbols_req)
        val etf_symbols = get_etf_symbols_rsp.symbolsList
        logger.info("获取到的 ETF symbols: ${etf_symbols.size}")
//         然后获取这些 symbols 的历史日线数据
        val get_etf_history_data_req = GetHistoryBarsReq.newBuilder().apply {
            setSymbols(etf_symbols.joinToString(","))
            frequency = "1d"
            skipSuspended = true
            startTime = trade_date.toString()
            endTime = trade_date.toString()
            adjust = 0
            adjustEndTime = "2025-05-30"
        }.build()

        val get_etf_history_data_rsp = history_api.getHistoryBars(get_etf_history_data_req)
        val etf_history_data = get_etf_history_data_rsp.dataList
        logger.info("获取到的 ETF 历史日线数据: ${etf_history_data.size}")

        etf_symbols.forEach { symbol ->
            val req = GetHistoryBarsReq.newBuilder().apply {
                symbols = symbol.info.symbol
                frequency = "1d"
                skipSuspended = true
                startTime = trade_date
                endTime = trade_date
                adjust = 1
                adjustEndTime = "2025-05-30"
            }.build()
            val rsp = history_api.getHistoryBars(req)
            if (rsp.dataList.isEmpty()) {
                logger.info("${symbol.info.symbol} 没有 ${trade_date} 历史数据")
            } else {
                val bar = rsp.dataList[0]
                logger.info("${symbol.info.symbol} 历史数据:\n${bar.pbToJsonStr()}")
            }
        }

//        // SHSE.501066
//        val symbol = "SHSE.501066"
//        val req = GetHistoryBarsReq.newBuilder().apply {
//            symbols = symbol
//            frequency = "1d"
//            skipSuspended = true
//            startTime = trade_date
//            endTime = trade_date
//            adjust = 1
//            adjustEndTime = "2025-05-30"
//        }.build()
//        val rsp = history_api.getHistoryBars(req)
//        if (rsp.dataList.isEmpty()) {
//            logger.info("${symbol} 没有 ${trade_date} 历史数据")
//        } else {
//            val bar = rsp.dataList[0]
//            logger.info("${symbol} 历史数据:\n${bar.pbToJsonStr()}")
//        }
    }

    @Test
    fun debug_002_test() {
        val req = InstrumentServiceProto.GetSymbolsReq.newBuilder().apply {
            addSymbols("SHSE.600190")
            tradeDate = "2025-06-26"
            secType1 = 1010
        }.build()

        val rsp = instrument_api.getSymbols(req)
        rsp.symbolsList.forEach {
            logger.info(it.tradeDate.toLocalDate().toString())
            logger.info("上市日期: ${it.info.listedDate.toLocalDate()}")
            logger.info("退市日期: ${it.info.delistedDate.toLocalDate()}")
            logger.info(it.pbToJsonStr())
        }
    }
}