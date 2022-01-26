package sz.cli.cmds

import com.github.ajalt.clikt.core.CliktCommand
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import nl.altindag.ssl.SSLFactory
import nl.altindag.ssl.util.PemUtils
import okhttp3.*
import okhttp3.Dns.Companion.SYSTEM
import org.apache.http.conn.ssl.DefaultHostnameVerifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Inet4Address
import java.net.InetAddress
import javax.net.ssl.SNIHostName


//
// Created by kk on 2021/9/20.
//
class SayHello : CliktCommand(name = "tls-client-app") {

    override fun run() {
        use_ktor_java()

    }

    fun use_okhttp() {
        val keyManager = PemUtils.loadIdentityMaterial("ssl/client.crt", "ssl/client.key")
        val trustManager = PemUtils.loadTrustMaterial("ssl/ca.crt")
        val sslFactory = SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustMaterial(trustManager)
            .withHostnameVerifier(DefaultHostnameVerifier())
            .build()

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslFactory.sslSocketFactory, trustManager)
            .hostnameVerifier(sslFactory.hostnameVerifier)
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return if (hostname == "api.myquant.cn") {
                        val ip1 = Inet4Address.getByName("192.168.3.106")
                        listOf(ip1)
                    } else {
                        SYSTEM.lookup(hostname)
                    }
                }
            })
            .build()

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.myquant.cn")
            .port(9999)
            .encodedPath("/api/sample/hello")
            .addQueryParameter("name", "刘德华")
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        logger.info(response.body?.string())
    }

    fun use_ktor_okhttp() {
        val keyManager = PemUtils.loadIdentityMaterial("ssl/client.crt", "ssl/client.key")
        val trustManager = PemUtils.loadTrustMaterial("ssl/ca.crt")
        val sslFactory = SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustMaterial(trustManager)
            .withHostnameVerifier(DefaultHostnameVerifier())
            .build()

        val httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    sslSocketFactory(sslFactory.sslSocketFactory, trustManager)
                    hostnameVerifier(sslFactory.hostnameVerifier)
                }

                addNetworkInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        logger.info("===================== NetworkInterceptor =====================")
                        val req = chain.request().newBuilder()
                            .header("Host", "apiv2.myquant.cn")
                            .build()
                        logger.debug(req.headers.toString())
                        return chain.proceed(req)
                    }
                })
            }
        }

        runBlocking {
            val res = httpClient.request<String>(urlString = "https://api.myquant.cn:9999/api/sample/hello") {
                method = HttpMethod.Get
                parameter("name", "KK")
            }

            println(res)
        }
    }

    fun use_ktor_java() {
        val keyManager = PemUtils.loadIdentityMaterial("ssl/client.crt", "ssl/client.key")
        val trustManager = PemUtils.loadTrustMaterial("ssl/ca.crt")
        val sslFactory = SSLFactory.builder()
            .withIdentityMaterial(keyManager)
            .withTrustMaterial(trustManager)
            .withHostnameVerifier(DefaultHostnameVerifier())
            .build()

        val httpClient = HttpClient(Java) {
            engine {
                config {
                    sslContext(sslFactory.sslContext)
                    val sslparams = sslFactory.sslParameters

                    sslparams.serverNames = listOf(SNIHostName("api.myquant.cn"))
                    sslParameters(sslparams)

                }
            }
            install(Logging) {
            }

        }

        runBlocking {
            val res = httpClient.request<String>(urlString = "https://127.0.0.1:9999/api/sample/hello") {
                method = HttpMethod.Get
                parameter("name", "KK")
            }

            println(res)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger("HelloCommand")!!
    }
}