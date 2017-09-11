package tasks

import jodd.datetime.JDateTime
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/8/27.
//
class SampleTask : Runnable{

    val time = JDateTime()

    override fun run() {
        Logger.debug("Run SampleTask", AnsiColor.YELLOW)
    }
}