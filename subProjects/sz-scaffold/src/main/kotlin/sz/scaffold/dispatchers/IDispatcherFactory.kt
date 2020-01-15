package sz.scaffold.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

//
// Created by kk on 2019-05-01.
//
interface IDispatcherFactory {

    fun build(): CoroutineDispatcher

}