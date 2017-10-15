package sz.scaffold.concurrent

import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.Function
import java.util.function.Supplier

//
// Created by kk on 2017/9/24.
//
class VertxCompletableFuture<T>(val context: Context, future: CompletableFuture<T>? = null) : CompletableFuture<T>(), CompletionStage<T> {

    init {
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

    constructor() : this(Vertx.currentContext())

    constructor(vertx: Vertx, future: CompletableFuture<T>? = null) : this(vertx.orCreateContext, future)

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

    constructor(context: Context, future: Future<T>?) : this(context) {
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

    /**
     * Creates a new {@link VertxCompletableFuture} using the given context. This method is used to switch between
     * Vert.x contexts.
     *
     * @return the created {@link VertxCompletableFuture}
     */
    fun withContext(newContext: Context): VertxCompletableFuture<T> {
        val future = VertxCompletableFuture<T>(newContext)
        this.whenComplete { res, err ->
            if (err != null) {
                future.completeExceptionally(err)
            } else {
                future.complete(res)
            }
        }

        return future
    }

    fun withContext(vertx: Vertx? = null): VertxCompletableFuture<T> {
        if (vertx != null) {
            return withContext(vertx.orCreateContext)
        } else {
            return withContext(Vertx.currentContext())
        }
    }

    //<editor-fold desc="Composite Future implementation">
    override fun <U : Any?> thenApply(fn: Function<in T, out U>?): CompletableFuture<U> {
        return VertxCompletableFuture(context, super.thenApplyAsync(fn, context.Executor()))
    }

    override fun <U : Any?> thenApplyAsync(fn: Function<in T, out U>?): CompletableFuture<U> {
        return VertxCompletableFuture(context, super.thenApplyAsync(fn, context.Executor()))
    }

    //</editor-fold>

    companion object {

        /**
         * Creates a new {@link VertxCompletableFuture} from the given {@link Vertx} instance and given
         * {@link CompletableFuture}. The returned future uses the current Vert.x context, or creates a new one.
         * <p>
         * The created {@link VertxCompletableFuture} is completed successfully or not when the given completable future
         * completes successfully or not.
         *
         * @param vertx  the Vert.x instance
         * @param future the future
         * @param <T>    the type of the result
         * @return the new {@link VertxCompletableFuture}
         */
        fun <T> from(vertx: Vertx, future: CompletableFuture<T>?): VertxCompletableFuture<T> {
            return VertxCompletableFuture(vertx, future)
        }

        /**
         * Creates a new {@link VertxCompletableFuture} from the given {@link Context} instance and given
         * {@link Future}. The returned future uses the current Vert.x context, or creates a new one.
         * <p>
         * The created {@link VertxCompletableFuture} is completed successfully or not when the given future
         * completes successfully or not.
         *
         * @param vertx  the Vert.x instance
         * @param future the Vert.x future
         * @param <T>    the type of the result
         * @return the new {@link VertxCompletableFuture}
         */
        fun <T> from(vertx: Vertx, future: Future<T>?): VertxCompletableFuture<T> {
            return VertxCompletableFuture(vertx, future)
        }

        /**
         * Creates a {@link VertxCompletableFuture} from the given {@link Context} and {@link CompletableFuture}.
         * <p>
         * The created {@link VertxCompletableFuture} is completed successfully or not when the given future
         * completes successfully or not. The completion is called on the given {@link Context}, immediately if it is
         * already executing on the right context, asynchronously if not.
         *
         * @param context the context
         * @param future  the future
         * @param <T>     the type of result
         * @return the creation {@link VertxCompletableFuture}
         */
        fun <T> from(context: Context, future: CompletableFuture<T>?): VertxCompletableFuture<T> {
            return VertxCompletableFuture(context, future)
        }

        /**
         * Creates a new {@link VertxCompletableFuture} from the given {@link Context} instance and given
         * {@link Future}. The returned future uses the current Vert.x context, or creates a new one.
         * <p>
         * The created {@link VertxCompletableFuture} is completed successfully or not when the given future
         * completes successfully or not. The created {@link VertxCompletableFuture} is completed successfully or not
         * when the given future completes successfully or not. The completion is called on the given {@link Context},
         * immediately if it is already executing on the right context, asynchronously if not.
         *
         * @param context the context
         * @param future  the Vert.x future
         * @param <T>     the type of the result
         * @return the new {@link VertxCompletableFuture}
         */
        fun <T> from(context: Context, future: Future<T>?): VertxCompletableFuture<T> {
            return VertxCompletableFuture(context, future)
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the current Vert.x
         * {@link Context} with the value obtained by calling the given Supplier.
         * <p>
         * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
         * executor, but use the Vert.x context.
         *
         * @param context  the context in which the supplier is executed.
         * @param supplier a function returning the value to be used to complete the returned CompletableFuture
         * @param <T>      the function's return type
         * @return the new CompletableFuture
         */
        fun <T> supplyAsync(context: Context, supplier: Supplier<T>): VertxCompletableFuture<T> {
            val future = VertxCompletableFuture<T>(context)
            context.runOnContext {
                try {
                    future.complete(supplier.get())
                } catch (ex: Exception) {
                    future.completeExceptionally(ex)
                }
            }
            return future
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the current Vert.x
         * {@link Context} with the value obtained by calling the given Supplier.
         * <p>
         * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
         * executor, but use the Vert.x context.
         *
         * @param vertx    the Vert.x instance
         * @param supplier a function returning the value to be used to complete the returned CompletableFuture
         * @param <T>      the function's return type
         * @return the new CompletableFuture
         */
        fun <T> supplyAsync(vertx: Vertx, supplier: Supplier<T>): VertxCompletableFuture<T> {
            return supplyAsync(vertx.orCreateContext, supplier)
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the
         * current Vert.x {@link Context} after it runs the given action.
         * <p>
         * This method is different from {@link CompletableFuture#runAsync(Runnable)} as it does not use a fork join
         * executor, but use the Vert.x context.
         *
         * @param context  the context
         * @param runnable the action to run before completing the returned CompletableFuture
         * @return the new CompletableFuture
         */
        fun runAsync(context: Context, runnable: () -> Unit): VertxCompletableFuture<Void> {
            val future = VertxCompletableFuture<Void>(context)
            context.runOnContext {
                try {
                    runnable()
                    future.complete(null)
                } catch (ex: Exception) {
                    future.completeExceptionally(ex)
                }
            }
            return future
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the
         * current Vert.x {@link Context} after it runs the given action.
         * <p>
         * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
         * executor, but use the Vert.x context.
         *
         * @param vertx    the Vert.x instance
         * @param runnable the action to run before completing the returned CompletableFuture
         * @return the new CompletableFuture
         */
        fun runAsync(vertx: Vertx, runnable: () -> Unit): VertxCompletableFuture<Void> {
            return runAsync(vertx.orCreateContext, runnable)
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a action running in the worker thread pool of
         * Vert.x
         * <p>
         * This method is different from {@link CompletableFuture#runAsync(Runnable)} as it does not use a fork join
         * executor, but the worker thread pool.
         *
         * @param context  the Vert.x context
         * @param runnable the action, when its execution completes, it completes the returned CompletableFuture. If the
         *                 execution throws an exception, the returned CompletableFuture is completed exceptionally.
         * @return the new CompletableFuture
         */
        fun runBlockingAsync(context: Context, runnable: () -> Unit): VertxCompletableFuture<Void> {
            val resultFuture = VertxCompletableFuture<Void>(context)
            context.executeBlocking<Void>({ future ->
                runnable()
                future.complete()
            }, { res ->
                if (res.succeeded()) {
                    resultFuture.complete(null)
                } else {
                    resultFuture.completeExceptionally(res.cause())
                }
            })

            return resultFuture
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a action running in the worker thread pool of
         * Vert.x
         * <p>
         * This method is different from {@link CompletableFuture#runAsync(Runnable)} as it does not use a fork join
         * executor, but the worker thread pool.
         *
         * @param vertx    the Vert.x instance
         * @param runnable the action, when its execution completes, it completes the returned CompletableFuture. If the
         *                 execution throws an exception, the returned CompletableFuture is completed exceptionally.
         * @return the new CompletableFuture
         */
        fun runBlockingAsync(vertx: Vertx, runnable: () -> Unit): VertxCompletableFuture<Void> {
            return runBlockingAsync(vertx.orCreateContext, runnable)
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the worker thread pool of
         * Vert.x
         * <p>
         * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
         * executor, but the worker thread pool.
         *
         * @param context  the context in which the supplier is executed.
         * @param supplier a function returning the value to be used to complete the returned CompletableFuture
         * @param <T>      the function's return type
         * @return the new CompletableFuture
         */
        fun <T> supplyBlockingAsync(context: Context, supplier: Supplier<T>): VertxCompletableFuture<T> {
            val resultFuture = VertxCompletableFuture<T>()
            context.executeBlocking<T>({ future ->
                future.complete(supplier.get())
            }, { res ->
                if (res.succeeded()) {
                    resultFuture.complete(res.result())
                } else {
                    resultFuture.completeExceptionally(res.cause())
                }
            })

            return resultFuture
        }

        /**
         * Returns a new CompletableFuture that is asynchronously completed by a task running in the worker thread pool of
         * Vert.x
         * <p>
         * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
         * executor, but the worker thread pool.
         *
         * @param vertx    the Vert.x instance
         * @param supplier a function returning the value to be used to complete the returned CompletableFuture
         * @param <T>      the function's return type
         * @return the new CompletableFuture
         */
        fun <T> supplyBlockingAsync(vertx: Vertx, supplier: Supplier<T>): VertxCompletableFuture<T> {
            return supplyBlockingAsync(vertx.orCreateContext, supplier)
        }

        /**
         * Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.  If any of
         * the given CompletableFutures complete exceptionally, then the returned CompletableFuture also does so, with a
         * CompletionException holding this exception as its cause.  Otherwise, the results, if any, of the given
         * CompletableFutures are not reflected in the returned CompletableFuture, but may be obtained by inspecting them
         * individually. If no CompletableFutures are provided, returns a CompletableFuture completed with the value
         * {@code null}.
         * <p>
         * <p>Among the applications of this method is to await completion
         * of a set of independent CompletableFutures before continuing a
         * program, as in: {@code CompletableFuture.allOf(c1, c2, c3).join();}.
         * <p>
         * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
         * stages into the Vert.x context.
         *
         * @param vertx   the Vert.x instance to retrieve the context
         * @param futures the CompletableFutures
         * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
         * @throws NullPointerException if the array or any of its elements are {@code null}
         */
        fun allOf(vertx: Vertx, vararg futures: CompletableFuture<*>): VertxCompletableFuture<Void> {
            return allOf(vertx.orCreateContext, *futures)
        }

        /**
         * Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.  If any of
         * the given CompletableFutures complete exceptionally, then the returned CompletableFuture also does so, with a
         * CompletionException holding this exception as its cause.  Otherwise, the results, if any, of the given
         * CompletableFutures are not reflected in the returned CompletableFuture, but may be obtained by inspecting them
         * individually. If no CompletableFutures are provided, returns a CompletableFuture completed with the value
         * {@code null}.
         * <p>
         * <p>Among the applications of this method is to await completion
         * of a set of independent CompletableFutures before continuing a
         * program, as in: {@code CompletableFuture.allOf(c1, c2, c3).join();}.
         * <p>
         * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
         * stages into the Vert.x context.
         *
         * @param context the context
         * @param futures the CompletableFutures
         * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
         * @throws NullPointerException if the array or any of its elements are {@code null}
         */
        fun allOf(context: Context, vararg futures: CompletableFuture<*>): VertxCompletableFuture<Void> {
            val all = CompletableFuture.allOf(*futures)
            return VertxCompletableFuture(context, all)
        }

        /**
         * Returns a new CompletableFuture that is completed when any of the given CompletableFutures complete, with the
         * same result. Otherwise, if it completed exceptionally, the returned CompletableFuture also does so, with a
         * CompletionException holding this exception as its cause.  If no CompletableFutures are provided, returns an
         * incomplete CompletableFuture.
         * <p>
         * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
         * stages into the Vert.x context.
         *
         * @param context the context
         * @param futures the CompletableFutures
         * @return a new CompletableFuture that is completed with the result or exception of any of the given
         * CompletableFutures when one completes
         * @throws NullPointerException if the array or any of its elements are {@code null}
         */
        fun anyOf(context: Context, vararg futures: CompletableFuture<*>): VertxCompletableFuture<Any> {
            val any = CompletableFuture.anyOf(*futures)
            return VertxCompletableFuture(context, any)
        }

        /**
         * Returns a new CompletableFuture that is completed when any of the given CompletableFutures complete, with the
         * same result. Otherwise, if it completed exceptionally, the returned CompletableFuture also does so, with a
         * CompletionException holding this exception as its cause.  If no CompletableFutures are provided, returns an
         * incomplete CompletableFuture.
         * <p>
         * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
         * stages into the Vert.x context.
         *
         * @param vertx   the Vert.x instance to retrieve the context
         * @param futures the CompletableFutures
         * @return a new CompletableFuture that is completed with the result or exception of any of the given
         * CompletableFutures when one completes
         * @throws NullPointerException if the array or any of its elements are {@code null}
         */
        fun anyOf(vertx: Vertx, vararg futures: CompletableFuture<*>): VertxCompletableFuture<Any> {
            return anyOf(vertx.orCreateContext, *futures)
        }

    }

    /**
     * Creates a Vert.x {@link Future} from the given {@link CompletableFuture} (that can be a
     * {@link VertxCompletableFuture}).
     *
     * @param future the future
     * @param <T>    the type of the result
     * @return the Vert.x future completed or failed when the given {@link CompletableFuture} completes or fails.
     */
    fun CompletableFuture<T>.toFuture(): Future<T> {
        val resultFuture = Future.future<T>()
        this.whenComplete { result, err ->
            if (err != null) {
                resultFuture.fail(err)
            } else {
                resultFuture.complete(result)
            }
        }

        return resultFuture
    }

    fun Context.Executor(): Executor {
        return Executor { command -> this.runOnContext { command.run() } }
    }

    fun Context.BlockingExecutor(): Executor {
        return Executor { command ->
            this.executeBlocking<Void>(
                    { future ->
                        try {
                            command.run()
                            future.complete()
                        } catch (ex: Exception) {
                            future.fail(ex)
                        }
                    },
                    null)
        }
    }

}