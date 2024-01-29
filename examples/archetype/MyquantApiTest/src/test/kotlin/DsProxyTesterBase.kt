import com.google.protobuf.Empty
import io.grpc.Channel
import myquant.proto.platform.data.data_dists.BasicDataQueryServiceGrpc
import myquant.proto.platform.data.data_dists.HistoryInnerServiceGrpc
import myquant.proto.platform.data.ds_fund.FundProxyServiceGrpc
import myquant.proto.platform.data.ds_instrument.InstrumentServiceGrpc
import myquant.proto.platform.data.fundamental.FundamentalServiceGrpc
import myquant.proto.platform.data.history.HistoryServiceGrpc
import myquant.proto.platform.health.HealthCheckServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

//
// Created by drago on 2023/9/12 012.
//
open class DsProxyTesterBase {
    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!
        val channel_factory: ChannelFactory

        val fundamental_api: FundamentalServiceGrpc.FundamentalServiceBlockingStub
        val instrument_api: InstrumentServiceGrpc.InstrumentServiceBlockingStub
        val fundProxy_api: FundProxyServiceGrpc.FundProxyServiceBlockingStub
        val history_api: HistoryServiceGrpc.HistoryServiceBlockingStub
        val innder_api: HistoryInnerServiceGrpc.HistoryInnerServiceBlockingStub
        val health_api: HealthCheckServiceGrpc.HealthCheckServiceBlockingStub
        val basic_data_dists_api: BasicDataQueryServiceGrpc.BasicDataQueryServiceBlockingStub

        init {
            channel_factory = test_env_channel_factory()
            val ds_proxy_channel = test_env_local_ds_proxy_channel()

            fundamental_api = FundamentalServiceGrpc.newBlockingStub(ds_proxy_channel)
            instrument_api = InstrumentServiceGrpc.newBlockingStub(ds_proxy_channel)
            fundProxy_api = FundProxyServiceGrpc.newBlockingStub(ds_proxy_channel)
            history_api = HistoryServiceGrpc.newBlockingStub(ds_proxy_channel)
            innder_api = HistoryInnerServiceGrpc.newBlockingStub(ds_proxy_channel)
            health_api = HealthCheckServiceGrpc.newBlockingStub(ds_proxy_channel)

            // 先ping一下, 保证已经创建好 grpc 连接
            health_api.ping(Empty.newBuilder().build())

            // 连接线上测试环境的数据分发服务
//            val dists_channel = channel_factory.getChannel("120.78.94.151", 7501)

            // 连接本地的数据分发服务
            val dists_channel = channel_factory.getChannel("127.0.0.1", 7513)
            basic_data_dists_api = BasicDataQueryServiceGrpc.newBlockingStub(dists_channel)
        }

        fun MeasureTime(block: () -> Unit) {
            val duration = measureTime(block)
            logger.info("耗时: ${duration.toString()}")
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

        // 先生生产环境
        private fun prod_env_channel_factory(): ChannelFactory {
            return ChannelFactory(
                gmHost = "discovery.myquant.cn",
                gmPort = 7061,
                plainToken = "16c784b8bf794616c5450de2bf1a4c8418b5e35d",
                orgCode = "myquant",
                siteId = "service-site-1"
            )
        }

        private fun test_env_local_ds_proxy_channel(): Channel {
            return channel_factory.getChannel("127.0.0.1", 7050)
        }
    }
}