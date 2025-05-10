import commons.toLocalDate
import myquant.proto.platform.data.fundamental.FundamentalServiceProto
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

//
// Created by drago on 2025/4/22 周二.
//
class DsCoreProdEnvTest : DsCoreProdEvnBase() {


    @Test
    @DisplayName("查询全部分红数据")
    fun GetDividends_all() {

        val req = FundamentalServiceProto.GetDividendsSnapshotReq.newBuilder().apply {
            date = "2024-01-01"
            endDate = LocalDate.now().toString()
        }.build()

        val rsp = fundamental_api.getDividendsSnapshot(req)
        logger.info("结果记录 ${rsp.dataCount} 条分红送配记录")

        val sortData = rsp.dataList.toMutableList().sortedBy { it.createdAt!!.toLocalDate() }
        logger.info("最早日期: ${sortData.first().createdAt.toLocalDate()}")
        logger.info("最晚日期: ${sortData.last().createdAt.toLocalDate()}")
    }
}