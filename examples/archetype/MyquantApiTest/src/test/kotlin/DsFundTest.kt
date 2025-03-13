import commons.pbToJsonStr
import myquant.proto.platform.data.ds_fund.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//
// Created by drago on 2024/10/28 周一.
//

@DisplayName("投研数据服务单元测试")
class DsFundTest : DsFundTesterBase() {

    @Test
    fun TestGetMoneyFlow_01() {
        val symbols = """SZSE.002986
SZSE.002730
SZSE.000526
SZSE.002970
SZSE.301291
SZSE.300305
SZSE.300021
SZSE.300140""".split("\n").joinToString(",")
        val req = FundStkServiceProto.GetMoneyFlowReq.newBuilder().apply {
            setSymbols(symbols)
//            tradeDate = "2024-10-24"
        }.build()

        val rsp = stk_api.getMoneyFlow(req)
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    fun TestPrettyPbJsonFormatter_01() {
        val symbols = """SZSE.002986
SZSE.002730
SZSE.000526
SZSE.002970
SZSE.301291
SZSE.300305
SZSE.300021
SZSE.300140""".split("\n").joinToString(",")
        val req = FundStkServiceProto.GetMoneyFlowReq.newBuilder().apply {
            setSymbols(symbols)
            tradeDate = "2024-10-24"
        }.build()

        logger.info("\n${req.pbToJsonStr()}")
    }

    @Test
    fun TestGetFinanceAudit_01() {
        val symbols = """
            SHSE.688567
            SHSE.603173
            SHSE.156690
            SHSE.156689
            SHSE.688207
            SZSE.301167
            SZSE.138469
            SZSE.139456
            SZSE.139455
            SZSE.139679
            SZSE.138468
            SZSE.139678
            SZSE.138424
            SHSE.179230
            SHSE.179229
            SZSE.138425
        """.trimIndent().split("\n").joinToString(",")

        val req = FundStkServiceProto.GetFinanceAuditReq.newBuilder().apply {
            setSymbols(symbols)
        }.build()

        val rsp = stk_api.getFinanceAudit(req)
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    fun TestGetFinanceForecast_01() {
        val symbols = """
            SHSE.688726
            SHSE.688726
            SHSE.688726
            SZSE.002475
            SZSE.002475
            SZSE.002475
            SHSE.688158
            SHSE.688550
            SHSE.688550
            SHSE.688550
            SHSE.A22369
            SZSE.300587
            SZSE.300587
            SZSE.A22458
            SZSE.A22458
            SZSE.A22458
            SHSE.A22367
            SHSE.A22369
            SHSE.A22369
            SHSE.A22367
        """.trimIndent().split("\n").joinToString(",")

        val req = FundStkServiceProto.GetFinanceForecastReq.newBuilder().apply {
            setSymbols(symbols)
        }.build()

        val rsp = stk_api.getFinanceForecast(req)
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    fun TestGetShare_01() {
        val req = FundFndServiceProto.GetShareReq.newBuilder().apply {
            fund = "SHSE.508002"
            startDate = "2024-10-01"
            endDate = "2024-10-28"
        }.build()

        val rsp = fnd_api.getShare(req)

        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }
    }

    @Test
    fun TestGetAnalysis_01() {
//        val symbols = """SHSE.110052
//SHSE.110062
//SHSE.110070
//SHSE.110077
//SHSE.110085
//SHSE.110092
//SHSE.110816
//SHSE.111004
//SHSE.111010
//SHSE.111015
//SHSE.111020
//SHSE.113033
//SHSE.113044
//SHSE.113049
//SHSE.113054
//SHSE.113060
//SHSE.113066
//SHSE.113524
//SHSE.113534
//SHSE.113549
//""".trimIndent().split("\n").joinToString(",")
//
        val req = FundBndServiceProto.GetAnalysisReq.newBuilder().apply {
            symbol = "SHSE.110077"
            startDate = "2024-10-01"
            endDate = "2024-10-28"
        }.build()

        val rsp = bnd_api.getAnalysis(req)
        rsp.dataList.forEach {
            logger.info(json_formatter.printToString(it))
        }

    }

    @Test
    fun createFixShellScript() {
        val destFile = "D:\\downloads\\fix_main_cont_contract.sh"
        val lines = StringBuilder()
        lines.appendLine("#!/bin/bash")
        lines.appendLine("")

        val endDate = LocalDate.parse("2024-11-01")
        var pDate = LocalDate.parse("2024-01-01")

        while (pDate.isBefore(endDate)) {
            // 过滤掉 周六 和 周日
            if (pDate.dayOfWeek == DayOfWeek.SATURDAY || pDate.dayOfWeek == DayOfWeek.SUNDAY) {
                pDate = pDate.plusDays(1)
                continue
            }

            lines.appendLine(
                "./gen-instrument --config=gen-ins.config.toml electContinuousContract --date=${
                    pDate.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                }"
            )
            pDate = pDate.plusDays(1)
        }

        File(destFile).writeText(lines.toString())
    }

    @Test
    fun GetFundamentalsCashflow_test1() {
        // stk_get_fundamentals_cashflow(symbol='SHSE.600000', rpt_type=2, data_type=None, start_date=None, end_date=None, fields='cash_pay_int_fee, cash_pay_fee,cash_pay_fin_leas', df=True)
        val req = FundStkServiceProto.GetFundamentalsCashflowReq.newBuilder().apply {
            symbol = "SHSE.600000"
            rptType = 2
            addAllFields(listOf("cash_pay_int_fee", "cash_pay_fee", "cash_pay_fin_leas"))
        }.build()

        val rsp = stk_api.getFundamentalsCashflow(req)
        logger.info("返回结果记录数量: ${rsp.dataCount}")
    }
}