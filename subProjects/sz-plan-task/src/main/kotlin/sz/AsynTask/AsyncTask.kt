package sz.AsynTask

import sz.scaffold.Application
import sz.scaffold.tools.json.toJsonPretty

//
// Created by kk on 17/9/10.
//

// 通常是那些不包含数据库事务的异步任务
class AsyncTask {

    var className: String = ""
    var data: String = ""

    fun send() {
        Application.vertx.eventBus().publish(AsyncTasksVerticle.address, this)
    }

    companion object {
        fun build(task: Runnable): AsyncTask {
            val asyncTask = AsyncTask()
            asyncTask.className = task.javaClass.name
            asyncTask.data = task.toJsonPretty()

            return asyncTask
        }
    }

}