package sz.scaffold.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread

//
// Created by kk on 2020/7/23.
//
class ForkJoinPoolDispatcher : IDispatcherFactory {

    val parallelism: Int by lazy {
        var defaultParallelism = Runtime.getRuntime().availableProcessors()
        if (defaultParallelism < 4) {
            defaultParallelism = 4
        }
        Application.config.getIntOrElse("app.httpServer.dispatcher.parallelism", defaultParallelism)
    }

    override fun build(): CoroutineDispatcher {
        return ForkJoinPool(parallelism, ThreadFactory(), null, true).asCoroutineDispatcher()
    }
}

class ThreadFactory : ForkJoinPool.ForkJoinWorkerThreadFactory {
    override fun newThread(pool: ForkJoinPool?): ForkJoinWorkerThread {
        val thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
        thread.name = " worker-${thread.name}"
        return thread
    }

}