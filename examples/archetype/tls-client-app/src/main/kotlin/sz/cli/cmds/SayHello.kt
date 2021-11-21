package sz.cli.cmds

import com.github.ajalt.clikt.core.CliktCommand
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import nl.altindag.ssl.SSLFactory
import nl.altindag.ssl.util.PemUtils
import org.slf4j.LoggerFactory


//
// Created by kk on 2021/9/20.
//
class SayHello : CliktCommand(name = "tls-client-app") {

    override fun run() {
        val keyManager = PemUtils.loadIdentityMaterial("ssl/client.crt", "ssl/client.key")
        val trustManager = PemUtils.loadTrustMaterial("ssl/ca.crt")
        val sslFactory = SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustMaterial(trustManager)
//            .withUnsafeHostnameVerifier()
            .build()

        val httpClient = HttpClient(Java) {
            engine {
                config {
                    sslContext(sslFactory.sslContext)
                    sslParameters(sslFactory.sslParameters)
                }
            }
        }

//        headers: {
//            'Content-Type': 'application/json; charset=utf-8',
//            'Host': 'api.myquant.cn'
//        },
        runBlocking {
            val res = httpClient.request<String>(urlString = "https://127.0.0.1:9999/api/sample/hello") {
                method = HttpMethod.Get
                parameter("name", "KK")
//                header("Content-Type", "application/json; charset=utf-8")
                header("Host", "api.myquant.cn")
            }

            println(res)
        }



    }

    companion object {
        val logger = LoggerFactory.getLogger("HelloCommand")!!
    }
}