package sz.interceptors

//
// Created by kk on 2019-05-06.
//

import io.ebean.annotation.TxIsolation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import sz.DB
import sz.SzEbeanConfig
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction
import java.util.concurrent.atomic.AtomicInteger

@WithAction(EbeanReadOnlyAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EbeanReadOnly(
    // 读写分离, 一个写数据库实例, 多个 只读 数据库实例, 这里为匹配只读数据库的 tag
    val dataSourceTag: String = "",
    val isolation: TxIsolation = TxIsolation.READ_COMMITED
)

class EbeanReadOnlyAction : Action<EbeanReadOnly>() {

    override suspend fun call(): Any? {
        return coroutineScope {
            val dsName = dataSourceOf(config.dataSourceTag)
            withContext(this.coroutineContext + DB.transactionCoroutineContext() + DB.dataSourceCoroutineContext(dsName)) {
                val db = DB.byDataSource(dsName)
                val tran = db.beginTransaction(config.isolation)
                try {
                    tran.isReadOnly = true
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

    companion object {

        private val count = AtomicInteger()

        private fun dataSourceOf(dataSourceTag: String): String {
            if (dataSourceTag.isEmpty()) {
                return ""
            }
            val dataSourceNames = SzEbeanConfig.dataSourceByTag(tag = dataSourceTag)
            if (dataSourceNames.size == 1) {
                return dataSourceNames.first()
            }
            val index = count.getAndIncrement() % dataSourceNames.size
            return dataSourceNames[index]
        }
    }
}