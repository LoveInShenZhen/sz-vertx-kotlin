import commons.toLocalDate
import myquant.proto.platform.data.fundamental.FundamentalServiceGrpc
import myquant.proto.platform.data.fundamental.GetTradingDatesReq
import myquant.rpc.client.ChannelFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

//
// Created by drago on 2023/9/11 011.
//

@DisplayName("老接口-交易日历测试")
class TradeDateTest :DsProxyTesterBase() {

    @Test
    @DisplayName("查询2023年的交易日历")
    fun GetTradingDates_2023() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "2023-01-01"
            endDate = "2023-12-31"
        }

        val rsp = fundamental_api.getTradingDates(req)
        assert(rsp.datesCount == 242)
    }

    @Test
    @DisplayName("查询2023年的交易日历, startDate 日期在 endDate 日期之后")
    fun GetTradingDates_wrong_order() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "2023-12-31"
            endDate = "2023-01-01"
        }

        val rsp = fundamental_api.getTradingDates(req)
        assert(rsp.datesCount == 0)
    }

    @Test
    @DisplayName("查询2029年的交易日历")
    fun GetTradingDate_2029() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "2029-01-01"
            endDate = "2029-12-31"
        }

        val rsp = fundamental_api.getTradingDates(req)
        logger.info("日历记录 4${rsp.datesCount} 条")
        assert(rsp.datesCount == 0)
    }

    @Test
    @DisplayName("查询1989年的交易日历")
    fun GetTradingDate_1989() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "1989-01-01"
            endDate = "1989-12-31"
        }

        val rsp = fundamental_api.getTradingDates(req)
        rsp.datesList.forEach {
            logger.info(it.toLocalDate().toString())
        }
        logger.info("日历记录 ${rsp.datesCount} 条")
        assert(rsp.datesCount == 0)
    }

    @Test
    @DisplayName("查询1989 ~ 1990年的交易日历")
    fun GetTradingDate_1989_to_1990() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "1989-01-01"
            endDate = "1990-12-31"
        }

        val rsp = fundamental_api.getTradingDates(req)
        rsp.datesList.forEach {
            logger.info(it.toLocalDate().toString())
        }
        logger.info("日历记录 ${rsp.datesCount} 条")
        assert(rsp.datesCount == 9)
    }

    @Test
    @DisplayName("查询2023 ~ 2024年的交易日历")
    fun GetTradingDate_2023_to_2024() {
        val req = GetTradingDatesReq {
            exchange = "SHSE"
            startDate = "2023-01-01"
            endDate = "2024-12-31"
        }

        val rsp = fundamental_api.getTradingDates(req)
//        rsp.datesList.forEach {
//            logger.info(it.toLocalDate().toString())
//        }
        logger.info("日历记录 ${rsp.datesCount} 条")
        assert(rsp.datesCount == 242)
    }


}