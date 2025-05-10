import myquant.proto.platform.data.fundamental.FundamentalServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory

//
// Created by drago on 2025/4/22 周二.
//
open class DsCoreProdEvnBase {
    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!
        val channel_factory: ChannelFactory
        val fundamental_api: FundamentalServiceGrpc.FundamentalServiceBlockingStub

        init {
            channel_factory = prod_env_channel_factory()

            fundamental_api = FundamentalServiceGrpc.newBlockingStub(channel_factory.getChannel("120.79.218.12", 7711))
                .withCompression("gzip")

//            fundamental_api =
//                FundamentalServiceGrpc.newBlockingStub(channel_factory.getChannel("47.106.123.34", 7521))
//                    .withCompression("gzip")
        }

        // 生产环境
        private fun prod_env_channel_factory(): ChannelFactory {
            return ChannelFactory(
                gmHost = "discovery.myquant.cn",
                gmPort = 7061,
                plainToken = "16c784b8bf794616c5450de2bf1a4c8418b5e35d",
                orgCode = "myquant",
                siteId = "service-site-1"
            )
        }
    }
}