package sz.scaffold.coroutines

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sz.scaffold.Application

//
// Created by kk on 2019-04-25.
//

fun CoroutineScope.launchOnVertx(vertx: Vertx = Application.vertx, block: suspend CoroutineScope.() -> Unit): Job {
    return this.launch(context = vertx.dispatcher(), block = block)
}