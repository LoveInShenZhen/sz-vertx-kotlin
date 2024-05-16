import com.google.protobuf.Empty
import com.googlecode.protobuf.format.JsonJacksonFormat
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
// Created by drago on 2024/4/29 周一.
//
open class ProdEnvBase {
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

        val json_formatter = JsonJacksonFormat()


        init {
            channel_factory = prod_env_channel_factory()
            val ds_proxy_channel = channel_factory.getChannel("ds-proxy-cloud-rpc")

            fundamental_api = FundamentalServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")
            instrument_api = InstrumentServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")
            fundProxy_api = FundProxyServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")
            history_api = HistoryServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")
            innder_api = HistoryInnerServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")
            health_api = HealthCheckServiceGrpc.newBlockingStub(ds_proxy_channel).withCompression("gzip")

            // 先ping一下, 保证已经创建好 grpc 连接
            health_api.ping(Empty.newBuilder().build())

            val dists_channel = channel_factory.getChannel("data-dists-rpc")

            basic_data_dists_api = BasicDataQueryServiceGrpc.newBlockingStub(dists_channel)
        }

        fun MeasureTime(block: () -> Unit) {
            val duration = measureTime(block)
            logger.info("耗时: ${duration.toString()}")
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