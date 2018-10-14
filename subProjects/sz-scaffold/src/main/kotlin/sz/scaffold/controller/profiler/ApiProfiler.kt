package sz.scaffold.controller.profiler

import sz.scaffold.Application
import sz.scaffold.aop.actions.Action
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getStringListOrEmpty
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger
import java.util.*

//
// Created by kk on 2018/10/9.
//
object ApiProfiler {

    private val enabled = Application.config.getBooleanOrElse("app.profiler.api.enabled", false)
    private val timeThreshold = Application.config.getIntOrElse("app.profiler.api.timeThreshold", 100)
    private val excludeRoutes = Application.config.getStringListOrEmpty("app.profiler.api.excludeRoutes").toSet()

    fun runAction(action: Action<*>): Any? {
        return if (enabled && excludeRoutes.contains(action.httpContext.request().path()).not()) {
            val befor = Date().time
            val result = action.call()
            val after = Date().time
            val timeConsuming = Math.abs(after - befor).toInt()
            if (timeConsuming > timeThreshold) {
                // 该 route 对应的 api 方法的执行时间超过了阈值, 应当记录到专属log中
                val record = ApiProfilerRecord.build(action.httpContext, timeConsuming)
                Logger.of("ApiProfiler").info(record.toShortJson())
            }
            result
        } else {
            action.call()
        }

    }


}