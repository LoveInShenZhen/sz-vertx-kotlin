package sz.interceptors

//
// Created by kk on 2019-04-27.
//

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import sz.DB
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import sz.scaffold.tools.logger.Logger

@WithAction(EbeanTransactionAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EbeanTransaction(val dataSource: String = "")

class EbeanTransactionAction : Action<EbeanTransaction>() {

    override suspend fun call(): Any? {
//        Logger.debug("将请求的协程上下文包装在Ebean的上下文中")
        return coroutineScope {
            withContext(this.coroutineContext + DB.ebeanCoroutineContext()) {
                val db = DB.byDataSource(config.dataSource)
                val tran = db.beginTransaction()
                try {
                    val result = delegate.call()
                    tran.commit()
                    return@withContext result
//                    Logger.debug("commit: $tran")
                } catch (e: Exception) {
                    tran.rollback(e)
//                    Logger.debug("rollback: $tran by ${e.message}")
                    throw e
                } finally {
                    tran.end()
                }
            }
        }
    }
}