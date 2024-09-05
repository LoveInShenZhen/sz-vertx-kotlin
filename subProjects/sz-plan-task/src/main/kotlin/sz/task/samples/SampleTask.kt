package sz.task.samples

import sz.logger.log
import java.util.*

//
// Created by kk on 17/8/27.
//
class SampleTask : Runnable{

    val id = UUID.randomUUID().toString()

    override fun run() {
        log.debug("Run SampleTask ($id) start. Thread name: ${Thread.currentThread().name}")
        Thread.sleep(3000)
        log.debug("Run SampleTask ($id) finished. Thread name: ${Thread.currentThread().name}")
    }
}