import commons.pbToJsonStr
import myquant.proto.platform.separate.SeparateBandwidthServiceProto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

//
// Created by drago on 2025/3/7 周五.
//

@DisplayName("独立带宽数据服务单元测试")
class SeparateDataTest : DsProxyTesterBase() {

    @Test
    fun LatestPriceTest() {
        MeasureTime {
            val symbols = """SHSE.000001
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
""".trimIndent().split("\n")
            val req = SeparateBandwidthServiceProto.LatestPriceReq.newBuilder().apply {
                addAllSymbols(symbols)
            }.build()

            val rsp = separate_data_api.latestPrice(req)
            logger.info(rsp.pbToJsonStr())
        }
    }

    @Test
    fun LatestPriceTest2() {
        MeasureTime {
            val symbols = "SHSE.000001"
            val req = SeparateBandwidthServiceProto.LatestPriceReq.newBuilder().apply {
                addSymbols(symbols)
            }.build()

            val rsp = separate_data_api.latestPrice(req)
            logger.info(rsp.pbToJsonStr())
        }
    }
}