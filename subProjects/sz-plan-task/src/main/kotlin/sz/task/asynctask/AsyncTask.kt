package sz.task.asynctask

import sz.scaffold.Application
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.json.toShortJson

//
// Created by kk on 17/9/10.
//

// 通常是那些不包含数据库事务的异步任务
@Suppress("MemberVisibilityCanBePrivate")
class AsyncTask {

    var className: String = ""
    var data: String = ""

    fun send() {
        Application.vertx.eventBus().send(AsyncTasksVerticle.address, this.toShortJson())
    }

    companion object {

        fun build(task: Runnable): AsyncTask {
            val asyncTask = AsyncTask()
            asyncTask.className = task.javaClass.name
            asyncTask.data = task.toJsonPretty()

            return asyncTask
        }

        fun submit(task: Runnable) {
            build(task).send()
        }
    }

}