import com.googlecode.protobuf.format.JsonJacksonFormat
import commons.PrettyPbJsonFormatter
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory
import myquant.proto.platform.data.ds_fund.FundStkServiceGrpc
import myquant.proto.platform.data.ds_fund.FundFndServiceGrpc
import myquant.proto.platform.data.ds_fund.FundBndServiceGrpc
import kotlin.time.measureTime


//
// Created by drago on 2024/10/28 周一.
//
open class DsFundTesterBase {

    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!
        val json_formatter = PrettyPbJsonFormatter()
        val channel_factory: ChannelFactory
        val stk_api: FundStkServiceGrpc.FundStkServiceBlockingStub
        val fnd_api: FundFndServiceGrpc.FundFndServiceBlockingStub
        val bnd_api: FundBndServiceGrpc.FundBndServiceBlockingStub

        init {
            channel_factory = test_env_channel_factory()
            // 连接本地的 ds-fund
//            val ds_fund_channel = channel_factory.getChannel("127.0.0.1", 7501)

            // 连接(掘金公版-阿里云后端测试环境) ds-fund
            val ds_fund_channel = channel_factory.getChannel("120.78.94.151", 7501)

            stk_api = FundStkServiceGrpc.newBlockingStub(ds_fund_channel).withCompression("gzip")
            fnd_api = FundFndServiceGrpc.newBlockingStub(ds_fund_channel).withCompression("gzip")
            bnd_api = FundBndServiceGrpc.newBlockingStub(ds_fund_channel).withCompression("gzip")

            json_formatter.defaultCharset = Charsets.UTF_8
        }

        fun measureTime(block: () -> Unit) {
            val duration = kotlin.time.measureTime(block)
            DistsTesterBase.Companion.logger.info("耗时: $duration")
        }

        // 线上测试环境
        private fun test_env_channel_factory(): ChannelFactory {
            return ChannelFactory(
                gmHost = "120.78.94.151",
                gmPort = 8201,
                plainToken = "b7aa8e2bb5093a200803a7844d2140ff2f605585",
                orgCode = "myquant",
                siteId = "kk-site"
            )
        }
    }
}