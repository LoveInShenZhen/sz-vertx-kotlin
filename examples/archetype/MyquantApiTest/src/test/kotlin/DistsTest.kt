import commons.toLocalDateTime
import myquant.proto.platform.data.data_dists.GetPatchRecordsReq
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

//
// Created by drago on 2024/1/23 023.
//

@DisplayName("数据分发服务单元测试")
class DistsTest : DistsTesterBase(){

    @Test
    fun GetPatchRecords_day_bar_SHSE() {
        val req = GetPatchRecordsReq {
            patchDataType = "daybar_by_day_meta"
            exchange = "SHSE"
            year = 2024
            lastCutimeBuilder.setSeconds(LocalDateTime.of(2004, 1, 18, 18, 55, 18).toEpochSecond(ZoneOffset.UTC))
        }

        val rsp = basic_data_dists_api.getPatchRecords(req)
        rsp.dataList.forEach {
            logger.info("${it.patchDataType} create_time: ${it.patchCreateTime.toLocalDateTime()} sha1: ${it.sha1} downloadUrl: ${it.downloadUrl}")
        }
    }

    @Test
    fun GetHistoryTickPatchRecord() {
        val req = GetPatchRecordsReq {
            patchDataType = "history_tick_meta"
            exchange = "SHSE"
            year = 0
            lastCutimeBuilder.setSeconds(LocalDateTime.of(2004, 1, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC))
        }

        val rsp = basic_data_dists_api.getPatchRecords(req)
        val record = rsp.dataList.filter { it.symbol == "SHSE.600000" }.first()
        logger.info(json_formatter.printToString(record))

    }
}