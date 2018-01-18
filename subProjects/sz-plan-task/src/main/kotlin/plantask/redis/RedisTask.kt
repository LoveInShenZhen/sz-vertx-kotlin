package plantask.redis

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jodd.datetime.JDateTime
import sz.scaffold.tools.json.JDateTimeWithMsJsonDeserializer
import sz.scaffold.tools.json.JDateTimeWithMsJsonSerializer
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2017/9/27.
//
class RedisTask {

    var id: Long = 0

    @JsonSerialize(using = JDateTimeWithMsJsonSerializer::class)
    @JsonDeserialize(using = JDateTimeWithMsJsonDeserializer::class)
    var planRunTime: JDateTime = RedisPlanTask.justRunTime

    var className: String = ""

    var jsonData: String = ""

    var tag: String = ""

    var error: String = ""

    var ordered: Boolean = false

    var singleton: Boolean = false

    fun run() {
        val task = loadTask()
        task.run()
    }

    private fun loadTask(): Runnable {
        return Json.fromJsonString(this.jsonData, Class.forName(this.className)) as Runnable
    }

    fun score(): Double {
        return ((planRunTime.convertToDate().time - RedisPlanTask.justRunTime.convertToDate().time)).toDouble()
    }

    fun delayInMs(now: JDateTime = JDateTime()): Long {
        val delay = Math.abs(planRunTime.convertToDate().time - now.convertToDate().time)
        return if (delay > 0) {
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