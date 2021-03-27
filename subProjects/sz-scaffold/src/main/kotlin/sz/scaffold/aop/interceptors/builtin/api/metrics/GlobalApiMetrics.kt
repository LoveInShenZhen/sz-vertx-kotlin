package sz.scaffold.aop.interceptors.builtin.api.metrics

import sz.scaffold.aop.interceptors.GlobalInterceptorBase
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.json.singleLineJson
import sz.scaffold.tools.logger.Logger

// app.httpServer.interceptors 中的配置
// includes 和 excludes 采用通配符匹配, excludes 比 includes 的优先级高
// Possible patterns allow to match single characters ('?') or any count of characters ('*').
// Wildcard characters can be escaped (by an '\'). When matching path, deep tree wildcard also can be used ('**').
// app {
//     httpServer {
//       interceptors = [
//         {
//             className = "sz.scaffold.aop.interceptors.builtin.api.metrics.GlobalApiMetrics"
//             config = {
//                 includes = ["/**"]
//                 excludes = []
//             }
//         }
//       ]
//     }
//   }
@Suppress("DuplicatedCode", "unused")
class GlobalApiMetrics : GlobalInterceptorBase() {

    private val logger = Logger.of("sz.api.metrics")

    override suspend fun whenMatch(): Any? {
        val record = MetricsRecord()
        try {
            val request = this.httpContext.request()

            record.http_method = request.method().name()
            record.path = request.path()
            record.query_string = request.query()
            record.headers = request.headers().map { Pair<String, String>(it.key, it.value) }.toMap()
            record.request_body = this.httpContext.getBodyAsString(contentCharset())

            record.start_time = System.currentTimeMillis()
            val result = delegate.call()
            record.end_time = System.currentTimeMillis()
            record.time_consuming = record.end_time - record.start_time
            record.http_status = this.httpContext.response().statusCode

            if (result is ReplyBase) {
                record.api_ret = result.ret
                record.api_err_msg = result.errmsg
            }

            logger.info(record.singleLineJson())

            return result
        } catch (ex: BizLogicException) {
            record.end_time = System.currentTimeMillis()
            record.time_consuming = record.end_time - record.start_time
            record.api_ret = ex.ErrCode
            record.api_err_msg = ex.message
            record.http_status = this.httpContext.response().statusCode

            logger.info(record.singleLineJson())
            throw ex
        } catch (ex: Exception) {
            record.end_time = System.currentTimeMillis()
            record.time_consuming = record.end_time - record.start_time
            record.api_ret = -1
            record.api_err_msg = ex.message
            record.http_status = this.httpContext.response().statusCode

            logger.info(record.singleLineJson())
            throw ex
        }
    }

    private fun contentCharset(): String {
        val contentType = this.httpContext.parsedHeaders().contentType()
        return if (contentType.rawValue().isNullOrBlank()) {
            "UTF-8"
        } else {
            val charsetParas = contentType.parameters().filter { it.key.equals("charset", ignoreCase = true) }.toList()
            if (charsetParas.isEmpty()) {
                "UTF-8"
            } else {
                charsetParas.first().second
            }
        }
    }
}