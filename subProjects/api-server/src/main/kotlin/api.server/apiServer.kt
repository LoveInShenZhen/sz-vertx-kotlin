@file:JvmName("MainApp")

package api.server

import sz.PlanTaskService
import sz.SzEbeanConfig
import sz.scaffold.Application


fun main(args: Array<String>) {
    SzEbeanConfig.loadConfig()

    Application.setupVertx()

    Application.regOnStartHandler(10) {
        PlanTaskService.Start()
    }

    Application.regOnStopHanlder(10) {
        PlanTaskService.Stop()
    }

    Application.run()

}

