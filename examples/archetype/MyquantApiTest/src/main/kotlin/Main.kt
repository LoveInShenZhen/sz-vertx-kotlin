import myquant.rpc.client.ChannelFactory
import myquant.proto.platform.data.fundamental.FundamentalServiceGrpc
import myquant.proto.platform.data.fundamental.GetTradingDatesReq
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

val logger = LoggerFactory.getLogger("app")!!

fun main(args: Array<String>) {
    println("Hello World!")
    logger.info("======================================")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    val factory = ChannelFactory(gmHost = "120.78.94.151",
        gmPort = 8201,
        plainToken = "b7aa8e2bb5093a200803a7844d2140ff2f605585",
        orgCode = "myquant",
        siteId = "kk-site")

    val channel = factory.getChannel("127.0.0.1", 7050)
    val rpcClient = FundamentalServiceGrpc.newBlockingStub(channel)

    val req = GetTradingDatesReq {
        startDate = "2020-01-01"
        endDate = "2020-12-31"
    }

    val rsp = rpcClient.getTradingDates(req)
    rsp.datesList.forEach{
        val localDTime = LocalDateTime.ofEpochSecond(it.seconds, it.nanos, ZoneOffset.ofHours(8))
        println(localDTime.toLocalDate().toString())
    }
}