@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package sz.ebean

import io.ebean.DB as Ebean
import io.ebean.Database as EbeanServer
import io.ebean.Finder
import sz.scaffold.tools.BizLogicException
import java.util.*
import kotlin.concurrent.getOrSet


object DB {

    fun byDataSource(dsName: String = ""): EbeanServer {
        if (dsName.isBlank()) {
            return if (SzEbeanConfig.defaultDatasourceReady) {
                Ebean.getDefault()
            } else {
                throw BizLogicException("Ebean default data source is not available.")
            }
        }
        if (SzEbeanConfig.ebeanServerConfigs.containsKey(dsName).not()) {
            throw BizLogicException("Ebean data source is not available or it is a invalid data source name [$dsName], please check application.conf")
        }
        return Ebean.byName(dsName)
    }

    /**
     * 根据线程上下文, 获取当前正在使用的 EbeanServer 实例
     */
    fun byContext(): EbeanServer {
        return byDataSource(currentDataSource())
    }

    /**
     * 根据当前Ebean工作线程上下文, 返回正在使用的 数据源的名称
     */
    fun currentDataSource(): String {
        val nameStack = dsNameStack.get()
        if (nameStack.empty()) {
            throw BizLogicException("The current thread [${Thread.currentThread().name}] has not initialized EBean data source settings, please call DB.setDataSourceContext(...) first.")
        }
        return nameStack.peek()
    }

    /**
     * 在当前Ebean工作线程上下文设置 dataSourceName
     */
    fun setDataSourceContext(newDsName: String) {
        val nameStack = dsNameStack.getOrSet { Stack() }
        nameStack.push(newDsName)
    }

    /**
     * 在当前Ebean工作线程上下文取消之前设置的 dataSourceName
     */
    fun unsetDataSourceContext() {
        val nameStack = dsNameStack.getOrSet { Stack() }
        if (nameStack.empty().not()) {
            nameStack.pop()
        }
    }

    /**
     * 初始化当前Ebean工作线程的记录数据源的上下文
     */
    fun initDataSourceContext() {
        dsNameStack.set(Stack())
    }

    fun clearDataSourceContext() {
        dsNameStack.remove()
    }

    private val dsNameStack = ThreadLocal<Stack<String>>()

    suspend fun <T> runTransactionAwait(dataSource: String = "", readOnly: Boolean = false, body: (ebeanServer: EbeanServer) -> T): T {
        return byDataSource(dataSource).runTransactionAwait(readOnly, body)
    }

    inline fun <reified I, reified T> finder(dsName: String): Finder<I, T> {
//        Logger.debug("create finder for ${T::class.java.name}, dataSource: $dsName")
        return Finder(T::class.java, dsName)
    }
}

