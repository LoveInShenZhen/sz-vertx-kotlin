package sz.scaffold.controller.profiler

import jodd.datetime.JDateTime
import sz.scaffold.Application
import sz.scaffold.aop.actions.Action
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2018/10/9.
//
object ApiProfiler {

    fun runAction(action: Action<*>): Any? {
        return if (Application.config.getBooleanOrElse("app.profiler.api.enabled", false)) {
            val timeThreshold = Application.config.getIntOrElse("app.profiler.api.timeThreshold", 100)
            val befor = JDateTime()
            val result = action.call()
            val after = JDateTime()
            val timeConsuming = Math.abs(after.timeInMillis - befor.timeInMillis).toInt()
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