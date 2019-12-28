package sz.scaffold.aop.interceptors.profiler

import sz.scaffold.aop.interceptors.GlobalInterceptorBase
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019-06-27.
//

// app.httpServer.interceptors 中的配置
// includes 和 excludes 采用通配符匹配, excludes 比 includes 的优先级高
// Possible patterns allow to match single characters ('?') or any count of characters ('*').
// Wildcard characters can be escaped (by an '\'). When matching path, deep tree wildcard also can be used ('**').
//{
//    className = "sz.scaffold.aop.interceptors.profiler.ApiProfiler"
//    config = {
//        timeThreshold = 100
//        includes = ["/**"]
//        excludes = []
//    }
//}

class ApiProfiler : GlobalInterceptorBase() {

    private val logger = Logger.of("sz.api.profiler")

    override suspend fun whenMatch(): Any? {
        val timeThreshold = this.config.getInteger("timeThreshold", 100)
        val befor = System.currentTimeMillis()
        val result = delegate.call()
        val after = System.currentTimeMillis()
        val timeConsuming = after - befor
        if (timeConsuming > timeThreshold) {
            // 该 route 对应的 api 方法的执行时间超过了阈值, 应当记录到专属log中
            val record = ApiProfilerRecord.build(this.httpContext, timeConsuming.toInt())
            logger.info(record.toShortJson())
        }

        return result
    }
}