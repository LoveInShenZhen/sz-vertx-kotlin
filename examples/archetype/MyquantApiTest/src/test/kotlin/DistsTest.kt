import commons.toLocalDateTime
import myquant.proto.platform.data.data_dists.BasicDataDistsServiceProto
import myquant.proto.platform.data.data_dists.DataDistsServiceProto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.floor

//
// Created by drago on 2024/1/23 023.
//

@DisplayName("数据分发服务单元测试")
class DistsTest : DistsProdEnvBase() {

    @Test
    fun GetPatchRecords_day_bar_SHSE() {
        val req = BasicDataDistsServiceProto.GetPatchRecordsReq.newBuilder().apply {
            patchDataType = "daybar_by_day_meta"
            exchange = "SHSE"
            year = 2024
            lastCutimeBuilder.setSeconds(LocalDateTime.of(2004, 1, 18, 18, 55, 18).toEpochSecond(ZoneOffset.UTC))
        }.build()

        val rsp = basic_data_dists_api.getPatchRecords(req)
        rsp.dataList.forEach {
            logger.info("${it.patchDataType} create_time: ${it.patchCreateTime.toLocalDateTime()} sha1: ${it.sha1} downloadUrl: ${it.downloadUrl}")
        }
    }

    @Test
    fun GetHistoryTickPatchRecord() {
        val req = BasicDataDistsServiceProto.GetPatchRecordsReq.newBuilder().apply {
            patchDataType = "history_tick_meta"
            exchange = "SHSE"
            year = 0
            lastCutimeBuilder.setSeconds(LocalDateTime.of(2004, 1, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC))
        }.build()

        val rsp = basic_data_dists_api.getPatchRecords(req)
        val record = rsp.dataList.filter { it.symbol == "SHSE.600000" }.first()
        logger.info(json_formatter.printToString(record))

    }

    @Test
    fun GetDownloadUrlOfHistoryData_test() {
        val req = DataDistsServiceProto.GetDownloadUrlOfHistoryDataReq.newBuilder().apply {
            dataType = "history-tick"
            symbol = "SHSE.600000"
            fromDate = "2025-04-18"
            toDate = "2025-04-18"
        }.build()

        var gm_count = 0
        var em_count = 0

        for (i in 1..100) {
            val rsp = dists_query_api.getDownloadUrlOfHistoryData(req)
            rsp.dataList.forEach {
                logger.info(it.downloadUrl)
                if (it.downloadUrl.contains("emgm3-data-dists")) {
                    em_count++
                } else {
                    gm_count++
                }
            }
        }

        logger.info("gm_count: $gm_count")
        logger.info("em_count: $em_count")
    }

    @Test
    fun testMathRound() {
        val num1 = 2.5
        val num2 = 3.5


        // 手动模拟 Math.round 的计算过程
        val result1 = floor(num1 + 0.5).toLong()
        val result2 = floor(num2 + 0.5).toLong()


        // 使用 Math.round 方法计算
        val roundResult1 = Math.round(num1)
        val roundResult2 = Math.round(num2)

        println("手动计算 2.5 四舍五入结果: $result1")
        println("Math.round(2.5) 结果: $roundResult1")

        println("手动计算 3.5 四舍五入结果: $result2")
        println("Math.round(3.5) 结果: $roundResult2")

        logger.info("Math.round(2.5) = ${Math.round(2.5)}")
        logger.info("Math.round(9 * 0.1) = ${Math.round(9 * 0.1)}")
        logger.info("Math.round(9 * 0.2) = ${Math.round(9 * 0.2)}")
    }
}