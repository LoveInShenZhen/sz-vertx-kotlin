package sz.scaffold.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import kotlin.math.max

//
// Created by kk on 2020/7/23.
//
class ForkJoinPoolDispatcher : IDispatcherFactory {

    val parallelism: Int by lazy {
        Application.config.getIntOrElse("app.httpServer.dispatcher.parallelism", max(16, Runtime.getRuntime().availableProcessors()))
    }

    override fun build(): CoroutineDispatcher {
        return ForkJoinPool(parallelism, ThreadFactory(), null, false).asCoroutineDispatcher()
    }
}

class ThreadFactory : ForkJoinPool.ForkJoinWorkerThreadFactory {
    override fun newThread(pool: ForkJoinPool?): ForkJoinWorkerThread {
        val thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
        thread.name = "http"
        return thread
    }

}