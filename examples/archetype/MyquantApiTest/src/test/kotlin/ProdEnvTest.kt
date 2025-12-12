import commons.pbToJsonStr
import myquant.proto.platform.data.data_dists.DataDistsServiceProto
import myquant.proto.platform.data.data_dists.DataInnerServiceProto
import myquant.proto.platform.data.history.HistoryServiceProto.GetHistoryTicksNReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

//
// Created by drago on 2024/4/29 周一.
//
class ProdEnvTest : ProdEnvBase() {

    @Test
    @DisplayName("BarsBatchQuery 接口流控测试")
    fun BarsBatchQuery_flow_ctrl_test() {
        for (i in 1..3100) {
            val req = DataInnerServiceProto.BatchQueryReq.newBuilder().apply {
                addExchangeSymbols(DataInnerServiceProto.ExchangeSymbols.newBuilder().apply {
                    exchange = "SZSE"
                    addSymbols("SZSE.000002")
                })

                startTime = "2024-04-30 09:30:00"
                endTime = "2024-04-30 09:45:00"
                frequency = "60s"
            }.build()

            val rsp = innder_api.barsBatchQuery(req)
            logger.info("查询第 ${i} 次完毕, 结果记录 ${rsp.dataCount} 条")
        }
    }

    @Test
    @DisplayName("HistoryTick 数据权限测试1")
    fun HistoryTick_data_auth_test_1() {
        val req = GetHistoryTicksNReq.newBuilder().apply {
            symbol = "SZSE.000001"
            count = 1000
            endTime = "2023-04-08 17:35:00"
        }.build()

        val rsp = history_api.getHistoryTicksN(req)
        logger.info("查询结果: ${rsp.dataCount}")
    }

    @Test
    @DisplayName("HistoryTick 数据权限测试2")
    fun HistoryTick_data_auth_test_2() {
        val req = DataDistsServiceProto.GetDownloadUrlOfHistoryDataReq.newBuilder().apply {
            symbol = "SZSE.000001"
            dataType = "history-tick"
            fromDate = "2023-04-07"
            toDate = "2023-04-07"
        }.build()

        val rsp = data_dists_api.getDownloadUrlOfHistoryData(req)
        logger.info("查询结果: ${rsp.pbToJsonStr()}")
    }
}