package plantask.redis

import jodd.datetime.JDateTime
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2017/9/27.
//
class RedisTask {

    var id: Long = 0

    var planRunTime: JDateTime = RedisPlanTask.justRunTime

    var className: String = ""

    var jsonData: String = ""

    var tag: String = ""

    var error: String = ""

    var ordered: Boolean = false

    fun run() {
        val task = loadTask()
        task.run()
    }

    private fun loadTask(): Runnable {
        return Class.forName(this.className).newInstance() as Runnable
    }

    fun score(): Double {
        return ((planRunTime.convertToDate().time - RedisPlanTask.justRunTime.convertToDate().time) / 1000).toDouble()
    }

    fun delayInMs(now: JDateTime = JDateTime()): Long {
        val delay = planRunTime.convertToDate().time - now.convertToDate().time
        return if (delay > 10) {
            delay
        } else {
            0
        }
    }

    companion object {

        fun parse(jsonStr: String): RedisTask? {
            try {
                return Json.fromJsonString(jsonStr, RedisTask::class.java)
            } catch (ex: Exception) {
                Logger.error("RedisTask parse failed.\n$jsonStr")
                return null
            }
        }
    }
}