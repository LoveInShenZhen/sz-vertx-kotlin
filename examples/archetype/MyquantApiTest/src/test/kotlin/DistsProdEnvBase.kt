import com.googlecode.protobuf.format.JsonJacksonFormat
import io.grpc.Channel
import myquant.proto.platform.data.data_dists.BasicDataQueryServiceGrpc
import myquant.proto.platform.data.data_dists.DistsQueryServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

//
// Created by drago on 2025/4/21 周一.
//
open class DistsProdEnvBase {
    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!
        val channel_factory: ChannelFactory
        val basic_data_dists_api: BasicDataQueryServiceGrpc.BasicDataQueryServiceBlockingStub
        val dists_query_api: DistsQueryServiceGrpc.DistsQueryServiceBlockingStub
        val json_formatter = JsonJacksonFormat()


        init {
            channel_factory = test_env_channel_factory()

            val dists_channel = aliyun_prod_channel()

            basic_data_dists_api = BasicDataQueryServiceGrpc.newBlockingStub(dists_channel).withCompression("gzip")
            dists_query_api = DistsQueryServiceGrpc.newBlockingStub(dists_channel).withCompression("gzip")

            json_formatter.defaultCharset = Charsets.UTF_8
        }

        fun MeasureTime(block: () -> Unit) {
            val duration = measureTime(block)
            logger.info("耗时: ${duration.toString()}")
        }

        fun aliyun_prod_channel(): Channel {
            logger.info("连接阿里云生产环境")
            return channel_factory.getChannel("120.79.218.12", 7513)
        }

        // 线上测试环境
        private fun test_env_channel_factory(): ChannelFactory {
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