package sz.scaffold.aop.interceptors.builtin.api.metrics

//
// Created by kk on 2019/12/30.
//
@Suppress("PropertyName")
class MetricsRecord {
    // the difference, measured in milliseconds, between the current time and
    // midnight, January 1, 1970 UTC.
    var start_time: Long = 0
    var end_time: Long = 0
    var time_consuming: Long = 0

    var http_method = ""

    var path = ""

    var query_string: String? = ""

    var headers = mapOf<String, String>()

    var request_body: String? = null

    var http_status: Int? = null

    var api_ret: Int? = null

    var api_err_msg: String? = null
}