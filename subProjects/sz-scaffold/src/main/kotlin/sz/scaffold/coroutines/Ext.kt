package sz.scaffold.coroutines

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import sz.scaffold.Application

//
// Created by kk on 2019-04-25.
//

fun CoroutineScope.launchOnVertx(vertx: Vertx = Application.vertx, block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(context = vertx.dispatcher(), block = block)
}

suspend fun <T> awaitEventWithTimeout(timeOut: Long, block: (h: Handler<T>) -> Unit): T = withTimeout(timeOut) {
    awaitEvent(block)
}

suspend fun <T> awaitResultWithTimeout(timeOut: Long, block: (h: Handler<AsyncResult<T>>) -> Unit): T = withTimeout(timeOut) {
    awaitResult(block)
}