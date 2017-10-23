package tasks

import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.util.*

//
// Created by kk on 17/8/27.
//
class SampleTask : Runnable{

    val id = UUID.randomUUID().toString()

    override fun run() {
        Logger.debug("Run SampleTask ($id) start. Threa Id: ${Thread.currentThread().id}", AnsiColor.YELLOW)
        Thread.sleep(3000)
        Logger.debug("Run SampleTask ($id) finished. Threa Id: ${Thread.currentThread().id}", AnsiColor.YELLOW)
    }
}