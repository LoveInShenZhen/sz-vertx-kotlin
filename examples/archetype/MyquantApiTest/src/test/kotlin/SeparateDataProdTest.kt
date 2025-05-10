import commons.pbToJsonStr
import myquant.proto.platform.data.ds_instrument.InstrumentServiceGrpc
import myquant.proto.platform.data.ds_instrument.InstrumentServiceProto
import myquant.proto.platform.separate.SeparateBandwidthServiceProto
import myquant.proto.platform.separate.SeparateDataServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

//
// Created by drago on 2025/4/11 周五.
//
class SeparateDataProdTest {

    val logger = LoggerFactory.getLogger("UnitTest")!!
    val channel_factory: ChannelFactory

    val separate_data_api: SeparateDataServiceGrpc.SeparateDataServiceBlockingStub
    val instrument_api: InstrumentServiceGrpc.InstrumentServiceBlockingStub

    init {
        channel_factory = channel_factory()
        val traefik_channel = channel_factory.getChannel("120.79.218.12", 7511)
        separate_data_api = SeparateDataServiceGrpc.newBlockingStub(traefik_channel).withCompression("gzip")
        instrument_api = InstrumentServiceGrpc.newBlockingStub(traefik_channel).withCompression("gzip")
    }

    private fun channel_factory(): ChannelFactory {
        return ChannelFactory(
            gmHost = "discovery.myquant.cn",
            gmPort = 7061,
            plainToken = "16c784b8bf794616c5450de2bf1a4c8418b5e35d",
            orgCode = "myquant",
            siteId = "service-site-1"
        )
    }

    @Test
    fun LatestPriceTest2() {
        val symbols = "SZSE.000001"
        val req = SeparateBandwidthServiceProto.LatestPriceReq.newBuilder().apply {
            addSymbols(symbols)
        }.build()

        val rsp = separate_data_api.latestPrice(req)
        logger.info(rsp.pbToJsonStr())
    }

    @Test
    fun instrument_test() {
        // 先查询沪市所有股票代码
        val req_symbols = InstrumentServiceProto.GetSymbolInfosReq.newBuilder().apply {
            secType1 = 1010
            addExchanges("SHSE")
            addExchanges("SZSE")
        }.build()

        logger.info("查询所有股票代码")
        val rsp_symbols = instrument_api.getSymbolInfos(req_symbols)
        logger.info("查询结果 ${rsp_symbols.symbolInfosList.count()} 支股票")
    }
}