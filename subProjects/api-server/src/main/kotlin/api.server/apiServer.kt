@file:JvmName("MainApp")

package api.server

import plantask.redis.RedisTaskLoader
import sz.AsynTask.AsyncTasksVerticle
import sz.PlanTaskService
import sz.SzEbeanConfig
import sz.scaffold.Application


fun main(args: Array<String>) {

    SzEbeanConfig.loadConfig()

    Application.setupVertx()

    Application.regOnStartHandler(10) {
        PlanTaskService.Start()
        AsyncTasksVerticle.deploy(Application.vertx)
        RedisTaskLoader.deploy(Application.vertx)
    }


    Application.regOnStopHanlder(10) {
        PlanTaskService.Stop()
        AsyncTasksVerticle.unDeploy()
        RedisTaskLoader.unDeploy()
    }

    Application.runHttpServer()

}

