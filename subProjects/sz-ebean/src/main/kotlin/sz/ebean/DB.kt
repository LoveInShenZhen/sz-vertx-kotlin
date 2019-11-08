@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package sz.ebean

import io.ebean.Ebean
import io.ebean.EbeanServer
import sz.scaffold.tools.BizLogicException
import java.util.*
import kotlin.concurrent.getOrSet


object DB {

    fun byDataSource(dsName: String = ""): EbeanServer {
        if (dsName.isBlank()) {
            return Ebean.getDefaultServer()
        }
        if (SzEbeanConfig.ebeanServerConfigs.containsKey(dsName).not()) {
            throw BizLogicException("""Invalid dataSource name: "$dsName", please check application.conf""")
        }
        return Ebean.getServer(dsName)!!
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
        return nameStack.peek()
    }

    /**
     * 在当前Ebean工作线程上下文设置 dataSourceName
     */
    fun setDataSourceContext(newDsName: String) {
        val nameStack = dsNameStack.getOrSet { Stack() }
        if (nameStack.empty()) {
            nameStack.push(newDsName)
        } else {
            val currentDs = nameStack.peek()
            if (currentDs != newDsName) {
                nameStack.push(newDsName)
            }
        }
    }

    /**
     * 在当前Ebean工作线程上下文取消之前设置的 dataSourceName
     */
    fun unsetDataSourceContext(currentDsName: String) {
        val nameStack = dsNameStack.getOrSet { Stack() }
        if (nameStack.empty().not()) {
            val currentDs = nameStack.peek()
            if (currentDs == currentDsName) {
                nameStack.pop()
            }
        }
    }

    /**
     * 初始化当前Ebean工作线程的记录数据源的上下文
     */
    fun initDataSourceContext() {
        val nameStack = Stack<String>()
        dsNameStack.set(nameStack)
    }

    fun clearDataSourceContext() {
        dsNameStack.remove()
    }

    private val dsNameStack = ThreadLocal<Stack<String>>()
}

