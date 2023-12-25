import com.google.protobuf.Empty
import myquant.proto.platform.data.data_dists.HistoryInnerServiceGrpc
import myquant.proto.platform.data.ds_fund.FundProxyServiceGrpc
import myquant.proto.platform.data.ds_instrument.InstrumentServiceGrpc
import myquant.proto.platform.data.fundamental.FundamentalServiceGrpc
import myquant.proto.platform.data.history.HistoryServiceGrpc
import myquant.proto.platform.health.HealthCheckServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory

//
// Created by drago on 2023/9/12 012.
//
open class DsProxyTesterBase {
    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!

        private val factory: ChannelFactory

        val fundamental_api: FundamentalServiceGrpc.FundamentalServiceBlockingStub
        val instrument_api: InstrumentServiceGrpc.InstrumentServiceBlockingStub
        val fundProxy_api: FundProxyServiceGrpc.FundProxyServiceBlockingStub
        val history_api: HistoryServiceGrpc.HistoryServiceBlockingStub
        val innder_api: HistoryInnerServiceGrpc.HistoryInnerServiceBlockingStub
        val health_api: HealthCheckServiceGrpc.HealthCheckServiceBlockingStub

        init {
            factory = ChannelFactory(
                gmHost = "120.78.94.151",
                gmPort = 8201,
                plainToken = "b7aa8e2bb5093a200803a7844d2140ff2f605585",
                orgCode = "myquant",
                siteId = "kk-site"
            )

            val channel = factory.getChannel("127.0.0.1", 7050)
            // 测试环境
//            val channel = factory.getChannel("120.79.180.133", 7521)

            fundamental_api = FundamentalServiceGrpc.newBlockingStub(channel)
            instrument_api = InstrumentServiceGrpc.newBlockingStub(channel)
            fundProxy_api = FundProxyServiceGrpc.newBlockingStub(channel)
            history_api = HistoryServiceGrpc.newBlockingStub(channel)
            innder_api = HistoryInnerServiceGrpc.newBlockingStub(channel)
            health_api = HealthCheckServiceGrpc.newBlockingStub(channel)

            // 先ping一下, 保证已经创建好 grpc 连接
            health_api.ping(Empty.newBuilder().build())
        }
    }
}