package sz.task.asynctask

import io.vertx.core.*
import io.vertx.core.eventbus.MessageConsumer
import jodd.exception.ExceptionUtil
import sz.logger.log
import sz.scaffold.tools.json.Json
import java.util.concurrent.Callable

//
// Created by kk on 17/9/10.
//

class AsyncTasksVerticle : AbstractVerticle() {

    private var consumer: MessageConsumer<String>? = null

    override fun start() {
        log.debug(
            "AsyncTasksVerticle start. context: ${this.context} Thread name: ${
                Thread.currentThread().name
            }"
        )
        consumer = this.vertx.eventBus().consumer(address) { message ->
            this.vertx.executeBlocking(Callable {
                log.debug("AsyncTasksVerticle received message. Thread name: ${Thread.currentThread().name} msg:\n${message.body()}")
                val asyncTask = Json.fromJsonString(message.body(), AsyncTask::class.java)
                val task = Json.fromJsonString(asyncTask.data, Class.forName(asyncTask.className)) as Runnable
                task.run()
            }).onComplete { result: AsyncResult<Unit> ->
                if (result.failed()) {
                    log.warn(ExceptionUtil.exceptionChainToString(result.cause()))
                }
            }

//            this.vertx.executeBlocking<Unit>({ future ->
////                log.debug("AsyncTasksVerticle received message. Threa Id: ${Thread.currentThread().id} msg:\n${message.body()}")
//                val asyncTask = Json.fromJsonString(message.body(), AsyncTask::class.java)
//                val task = Json.fromJsonString(asyncTask.data, Class.forName(asyncTask.className)) as Runnable
//                task.run()
//                future.complete()
//            }, false, { result ->
//                if (result.failed()) {
//                    log.warn(ExceptionUtil.exceptionChainToString(result.cause()))
//                }
//            })
        }
    }

    override fun stop() {
        if (consumer != null) {
            consumer!!.unregister()
        }
        log.debug("AsyncTasksVerticle stop")
    }

    companion object {

        const val address = "sz.app.asyncTask"
        private var deoloyId = ""
        private var vertxRef: Vertx? = null

        fun deploy(vertx: Vertx) {
            val options = DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)

            val verticle = AsyncTasksVerticle()
            vertx.deployVerticle(verticle, options) { res ->
                if (res.succeeded()) {
                    deoloyId = res.result()
                    vertxRef = vertx
                } else {
                    log.error("Deploy AsyncTasksVerticle failed.")
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