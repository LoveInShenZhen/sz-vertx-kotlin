package sz.interceptors

//
// Created by kk on 2019-04-27.
//

import io.ebean.annotation.TxIsolation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import sz.DB
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.tools.logger.Logger

@WithAction(EbeanTransactionAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EbeanTransaction(
    val dataSourceName: String = "",
    val readonly: Boolean = false,
    val isolation: TxIsolation = TxIsolation.READ_COMMITED)

class EbeanTransactionAction : Action<EbeanTransaction>() {

    override suspend fun call(): Any? {
        // 将请求的协程上下文包装在Ebean的上下文中
        return coroutineScope {
            withContext(this.coroutineContext + DB.transactionCoroutineContext() + DB.dataSourceCoroutineContext(config.dataSourceName)) {
                val db = DB.byDataSource(config.dataSourceName)
                val tran = db.beginTransaction(config.isolation)
                try {
                    tran.isReadOnly = config.readonly
                    val result = delegate.call()
                    tran.commit()
                    return@withContext result
                } catch (e: Exception) {
                    tran.rollback(e)
                    throw e
                } finally {
                    tran.end()
                }
            }
        }
    }
}