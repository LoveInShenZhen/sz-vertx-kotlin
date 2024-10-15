import com.googlecode.protobuf.format.JsonFormat
import com.googlecode.protobuf.format.JsonJacksonFormat
import commons.toJsonPretty
import commons.toLocalDate
import io.grpc.stub.MetadataUtils
import myquant.proto.platform.data.ds_fund.FndGetDividendReq
import myquant.proto.platform.data.ds_fund.FundFndServiceProto
import myquant.proto.platform.data.ds_fund.GetDividendReq
import myquant.proto.platform.data.ds_fund.GetSplitReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

//
// Created by drago on 2023/9/14 014.
//
class FundProxyServiceTest : DsProxyTesterBase() {

    @Test
    @DisplayName("查询股票分红送配")
    fun GetDividend_stk() {
        val req = GetDividendReq {
            symbol = "SHSE.601318"
            startDate = "1980-01-01"
            endDate = "2023-12-31"
        }

        val rsp = fundProxy_api.getDividend(req)
        assert(rsp.dataCount > 0)
        logger.info("查询股票 ${req.symbol} 在 ${req.startDate} ~ ${req.endDate} 期间的分红送配记录 ${rsp.dataCount} 条")
        rsp.dataList.forEach {
            logger.info("${it.symbol} ${it.exDate.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("查询基金分红记录")
    fun GetDividend_fund_dvd() {
        val req = FndGetDividendReq{
            fund = "SZSE.161911"
            startDate = "1980-01-01"
            endDate = "2023-12-31"
        }

        val rsp = fundProxy_api.fndGetDividend(req)
        assert(rsp.dataCount > 0)
        logger.info("查询基金 ${req.fund} 在 ${req.startDate} ~ ${req.endDate} 期间的分红记录 ${rsp.dataCount} 条")
        rsp.dataList.forEach {
            logger.info("${it.fund} ${it.exDvdDate.toLocalDate()}")
        }
    }

    @Test
    @DisplayName("查询基金拆分记录")
    fun GetSplit_fund() {
        val req = GetSplitReq{
            fund = "SHSE.516160"
            startDate = "2024-01-01"
            endDate = "2024-12-31"
        }

        val rsp = fundProxy_api.getSplit(req)

        assert(rsp.dataCount > 0)
        logger.info("查询基金 ${req.fund} 在 ${req.startDate} ~ ${req.endDate} 期间的拆分记录 ${rsp.dataCount} 条")
        val jsonFormatter = JsonFormat()
        rsp.dataList.forEach {
//            logger.info("${it.fund} ${it.exDateClose.toLocalDate()}")
            logger.info(jsonFormatter.printToString(it))
        }
    }
}