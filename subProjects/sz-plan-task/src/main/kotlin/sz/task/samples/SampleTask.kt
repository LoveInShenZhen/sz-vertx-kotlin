package sz.task.samples

import sz.logger.log
import java.util.*

//
// Created by kk on 17/8/27.
//
class SampleTask : Runnable{

    val id = UUID.randomUUID().toString()

    override fun run() {
        log.debug("Run SampleTask ($id) start. Threa Id: ${Thread.currentThread().threadId()}")
        Thread.sleep(3000)
        log.debug("Run SampleTask ($id) finished. Threa Id: ${Thread.currentThread().threadId()}")
    }
}