package sz.scaffold.dispatchers

import jodd.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.tools.logger.Logger
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

//
// Created by kk on 2019-05-01.
//
class CachedThreadPoolDispatcher : IDispatcherFactory {

    override fun build(): CoroutineDispatcher {
        return dispatcherInstence
    }

    companion object {
        val corePoolSize: Int
            get() {
                return Application.config.getIntOrElse("app.httpServer.dispatcher.corePoolSize", Runtime.getRuntime().availableProcessors().coerceAtLeast(4))
            }

        val maximumPoolSize: Int
            get() {
                return Application.config.getIntOrElse("app.httpServer.dispatcher.maximumPoolSize", corePoolSize * 2)
            }

        // 单位: SECONDS
        val keepAliveTime: Long
            get() {
                return Application.config.getIntOrElse("app.httpServer.dispatcher.keepAliveTime", 60).toLong()
            }

        private val dispatcherInstence: CoroutineDispatcher by lazy {
            Logger.debug("Create CachedThreadPoolDispatcher: corePoolSize: $corePoolSize, maximumPoolSize: $maximumPoolSize, keepAliveTime: $keepAliveTime seconds")
            ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                ThreadFactoryBuilder().setNameFormat("cached-worker-%d").get()).asCoroutineDispatcher()
        }
    }
}