package sz.AsynTask

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import jodd.exception.ExceptionUtil
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/9/10.
//

class AsyncTasksVerticle : AbstractVerticle() {

    private var consumer: MessageConsumer<String>? = null

    override fun start() {
        Logger.debug("AsyncTasksVerticle start. context: ${this.context} Threa Id: ${Thread.currentThread().id}")
        consumer = this.vertx.eventBus().consumer<String>(address) { message ->
            this.vertx.executeBlocking<Unit>({ future ->
//                Logger.debug("AsyncTasksVerticle received message. Threa Id: ${Thread.currentThread().id} msg:\n${message.body()}")
                val asyncTask = Json.fromJsonString(message.body(), AsyncTask::class.java)
                val task = Json.fromJsonString(asyncTask.data, Class.forName(asyncTask.className)) as Runnable
                task.run()
                future.complete()
            }, false, { result ->
                if (result.failed()) {
                    Logger.warn(ExceptionUtil.exceptionChainToString(result.cause()))
                }
            })
        }
    }

    override fun stop() {
        if (consumer != null) {
            consumer!!.unregister()
        }
        Logger.debug("AsyncTasksVerticle stop")
    }

    companion object {

        const val address = "sz.app.asyncTask"
        private var deoloyId = ""
        private var vertxRef: Vertx? = null

        fun deploy(vertx: Vertx) {
            val options = DeploymentOptions()
            options.isWorker = true
//            options.isMultiThreaded = true
            val verticle = AsyncTasksVerticle()
            vertx.deployVerticle(verticle, options) { res ->
                if (res.succeeded()) {
                    deoloyId = res.result()
                    vertxRef = vertx
                } else {
                    Logger.error("Deploy AsyncTasksVerticle failed.")
                    vertx.close()
                }
            }
        }

        fun unDeploy() {
            if (deoloyId.isNotBlank()) {
                vertxRef!!.undeploy(deoloyId)
            }
        }
    }
}