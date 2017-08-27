package tasks

import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 17/8/27.
//
class SampleTask : Runnable{
    override fun run() {
        Logger.debug("Run SampleTask", AnsiColor.YELLOW)
    }
}