import myquant.proto.platform.data.data_dists.BatchQueryReq
import myquant.proto.platform.data.data_dists.DataInnerServiceProto
import myquant.proto.platform.data.data_dists.ExchangeSymbols
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
            val req = BatchQueryReq{
                ExchangeSymbols {
                    exchange = "SZSE"
                    addSymbols("SZSE.000002")
                }

                startTime = "2024-04-30 09:30:00"
                endTime = "2024-04-30 09:45:00"
                frequency = "60s"
            }

            val rsp = innder_api.barsBatchQuery(req)
            logger.info("查询第 ${i} 次完毕, 结果记录 ${rsp.dataCount} 条")
        }
    }

}