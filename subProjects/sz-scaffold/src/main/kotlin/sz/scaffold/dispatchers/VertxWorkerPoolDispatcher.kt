package sz.scaffold.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import sz.scaffold.Application

//
// Created by kk on 2019-05-01.
//
class VertxWorkerPoolDispatcher : IDispatcherFactory {
    override fun build(): CoroutineDispatcher {
        return dispatcherInstence
    }

    companion object {
        private val dispatcherInstence: CoroutineDispatcher by lazy {
            Application.workerPool.asCoroutineDispatcher()
        }
    }
}