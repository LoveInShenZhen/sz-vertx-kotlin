package sz.cli.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.grpc.ManagedChannelBuilder
import myquant.proto.client.TokenUpdater
import org.slf4j.LoggerFactory
import sz.cli.config.myquantConfig

//
// Created by kk on 2021/9/20.
//
class Hello : CliktCommand() {
//    val count: Int by option(help="Number of greetings").int().default(1)
//    val name: String by option(help="The person to greet").default("KK")

    override fun run() {
//        repeat(count) {
//            echo("Hello $name!")
//        }
        logger.info("Hello myquant")

        val channel = ManagedChannelBuilder.forAddress(myquantConfig.entry.host, myquantConfig.entry.port)
            .usePlaintext()
            .enableRetry()
            .maxRetryAttempts(5)
            .maxInboundMessageSize(1024 * 1024 * 32)
            .build()

        val tokenUpdater = TokenUpdater(channel, myquantConfig.entry.token, myquantConfig.entry.orgCode)
        tokenUpdater.InitEncryptedToken()

        Thread.sleep(1000 * 60 *11)

        logger.info("EncryptedToken : ${tokenUpdater.EncryptedToken}")

    }

    companion object {
        val logger = LoggerFactory.getLogger("app")
    }
}