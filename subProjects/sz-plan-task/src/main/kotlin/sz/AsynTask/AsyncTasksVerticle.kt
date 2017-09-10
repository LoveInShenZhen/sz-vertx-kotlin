package sz.AsynTask

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import jodd.exception.ExceptionUtil
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/10.
//
class AsyncTasksVerticle : AbstractVerticle() {

    override fun start() {
        Logger.debug("AsyncTasksVerticle start")
        this.vertx.eventBus().consumer<AsyncTask>(address) { message ->
            try {
                val asyncTask = message.body()
                val task = Json.fromJsonString(asyncTask.data, Class.forName(asyncTask.className)) as Runnable

                this.vertx.executeBlocking<Unit>({ future ->
                    task.run()
                    future.complete()
                }, { result ->
                    if (result.failed()) {
                        Logger.warn(ExceptionUtil.exceptionChainToString(result.cause()))
                    }
                })

                task.run()
            } catch (ex: Exception) {
                Logger.warn(ExceptionUtil.exceptionChainToString(ex))
            }
        }
    }

    override fun stop() {
        Logger.debug("AsyncTasksVerticle stop")
    }

    companion object {

        val address = "sz.app.asyncTask"

        fun deploy(vertx: Vertx) {
            val options = DeploymentOptions()
            options.isWorker = true

            vertx.deployVerticle(AsyncTasksVerticle(), options)
        }
    }
}