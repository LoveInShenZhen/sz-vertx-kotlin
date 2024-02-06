import com.google.protobuf.Empty
import com.googlecode.protobuf.format.JsonJacksonFormat
import io.grpc.Channel
import myquant.proto.platform.data.data_dists.BasicDataQueryServiceGrpc
import myquant.proto.platform.health.HealthCheckServiceGrpc
import myquant.rpc.client.ChannelFactory
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

//
// Created by drago on 2024/1/24 024.
//
open class DistsTesterBase {
    companion object {
        val logger = LoggerFactory.getLogger("UnitTest")!!
        val channel_factory: ChannelFactory
        val basic_data_dists_api: BasicDataQueryServiceGrpc.BasicDataQueryServiceBlockingStub
        val json_formatter = JsonJacksonFormat()


        init {
            channel_factory = test_env_channel_factory()

            val dists_channel = local_data_dists_channel()

            basic_data_dists_api = BasicDataQueryServiceGrpc.newBlockingStub(dists_channel).withCompression("gzip")

            json_formatter.defaultCharset = Charsets.UTF_8
        }

        fun MeasureTime(block: () -> Unit) {
            val duration = measureTime(block)
            logger.info("耗时: ${duration.toString()}")
        }

        fun local_data_dists_channel(): Channel {
            return channel_factory.getChannel("127.0.0.1", 7513)
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