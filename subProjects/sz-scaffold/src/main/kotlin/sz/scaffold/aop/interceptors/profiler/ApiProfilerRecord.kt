package sz.scaffold.aop.interceptors.profiler

import io.vertx.ext.web.RoutingContext

//
// Created by kk on 2018/10/9.
//
class ApiProfilerRecord {

    // 耗时, 单位: ms
    var timeConsuming = 0

    // api 对应的 route path
    var route = ""

    // api 对应的 http method
    var httpMethod = ""

    var queryString: String? = ""

    var headers = mapOf<String, String>()

    var rawBody: String? = ""

    companion object {

        fun build(httpContext: RoutingContext, timeConsuming: Int): ApiProfilerRecord {
            val record = ApiProfilerRecord()
            record.timeConsuming = timeConsuming
            record.route = httpContext.request().path()
            record.httpMethod = httpContext.request().method().name
            record.queryString = httpContext.request().query()
            record.headers = httpContext.request().headers().map { Pair<String, String>(it.key, it.value) }.toMap()
            record.rawBody = httpContext.getBodyAsString(Charsets.UTF_8.name())

            return record
        }
    }
}