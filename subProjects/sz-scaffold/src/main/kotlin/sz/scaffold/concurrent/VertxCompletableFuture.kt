package sz.scaffold.concurrent

import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

//
// Created by kk on 2017/9/24.
//
class VertxCompletableFuture<T>(val context: Context, future: CompletableFuture<T>?) : CompletableFuture<T>(), CompletionStage<T> {

    private val executor: Executor

    init {
        executor = Executor { command -> command.run() }
        if (future != null) {
            future.whenComplete { res, err: Throwable? ->
                if (err != null) {
                    this.completeExceptionally(err)
                } else {
                    this.complete(res)
                }
            }
        }
    }

    constructor(vertx: Vertx) : this(vertx.orCreateContext, null)

    constructor(vertx: Vertx, future: CompletableFuture<T>) : this(vertx.orCreateContext, future)

    constructor(vertx: Vertx, future: Future<T>?) : this(vertx) {
        if (future != null) {
            future.setHandler { event: AsyncResult<T> ->
                if (event.succeeded()) {
                    this.complete(event.result())
                } else {
                    this.completeExceptionally(event.cause())
                }
            }
        }
    }
}