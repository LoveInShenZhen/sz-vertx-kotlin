import commons.pbToJsonStr
import myquant.proto.platform.separate.SeparateBandwidthServiceProto
import myquant.proto.platform.separate.SeparateDataServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

//
// Created by drago on 2025/4/11 周五.
//
class SeparateDataOnlineTestEnvTest {
    val logger = LoggerFactory.getLogger("UnitTest")!!
    val channel_factory: ChannelFactory

    val separate_data_api: SeparateDataServiceGrpc.SeparateDataServiceBlockingStub

    init {
        channel_factory = channel_factory()
        val ds_proxy_channel = channel_factory.getChannel("120.79.180.133", 7521)
        separate_data_api = SeparateDataServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")

    }

    private fun channel_factory(): ChannelFactory {
        return ChannelFactory(
            gmHost = "120.78.94.151",
            gmPort = 8201,
            plainToken = "b7aa8e2bb5093a200803a7844d2140ff2f605585",
            orgCode = "myquant",
            siteId = "kk-site"
        )
    }

    @Test
    fun LatestPriceTest2() {
        val req = SeparateBandwidthServiceProto.LatestPriceReq.newBuilder().apply {
            addSymbols("CFFEX.IM2506")
//            addSymbols("CFFEX.IM")
//            addSymbols("CFFEX.IM02")
        }.build()

        val rsp = separate_data_api.latestPrice(req)
        logger.info(rsp.pbToJsonStr())
    }

}