package sz.coroutines

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private data class ThreadLocalKey(private val threadLocal: ThreadLocal<*>) : CoroutineContext.Key<AutoThreadLocalElement<*>>

class AutoThreadLocalElement<T>(
    private var value: T,
    private val threadLocal: ThreadLocal<T>
) : ThreadContextElement<T> {
    override val key: CoroutineContext.Key<*> = ThreadLocalKey(threadLocal)

    override fun updateThreadContext(context: CoroutineContext): T {
        val oldState = threadLocal.get()
        threadLocal.set(value)
        return oldState
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: T) {
        value = threadLocal.get()
        threadLocal.set(oldState)
    }

    // this method is overridden to perform value comparison (==) on key
    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
        return if (this.key == key) EmptyCoroutineContext else this
    }

    // this method is overridden to perform value comparison (==) on key
    override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
        @Suppress("UNCHECKED_CAST")
        if (this.key == key) this as E else null

    override fun toString(): String = "ThreadLocal(value=$value, threadLocal = $threadLocal)"
}

fun <T> ThreadLocal<T>.asAutoContextElement(value: T = get()): AutoThreadLocalElement<T> =
    AutoThreadLocalElement(value, this)
