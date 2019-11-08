@file:Suppress("DuplicatedCode")

package sz.ebean

import io.ebean.EbeanServer
import io.ebean.SqlRow
import io.ebean.TxScope
import io.ebean.annotation.TxIsolation
import jodd.bean.BeanUtil
import jodd.datetime.JDateTime
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.future.await
import sz.scaffold.tools.BizLogicException
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

//
// Created by kk on 2019/11/7.
//

fun BigDecimal?.safeValue(): BigDecimal {
    return this ?: BigDecimal.ZERO
}

/**
 * just working on mysql
 */
fun EbeanServer.tableExists(tableName: String): Boolean {
    val rows = this.createSqlQuery("SHOW TABLES").findList()
    val count = rows.count { it.values.first() == tableName }
    return count > 0
}

suspend fun EbeanServer.runInTransactionAwait(readOnly: Boolean = false, body: () -> Unit) {
    val ebeanServer = this
    val worker = SzEbeanConfig.workerOf(ebeanServer.name)
    val ebeanFuture = CompletableFuture.runAsync(Runnable {
        DB.initDataSourceContext()
        DB.setDataSourceContext(ebeanServer.name)

        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        ebeanServer.beginTransaction(txScope).use { transaction ->
            body()
            transaction.commit()
        }
    }, worker)

    ebeanFuture.await()
}

suspend fun <T> EbeanServer.callInTransactionAwait(readOnly: Boolean = false, body: () -> T): T {
    val ebeanServer = this
    val worker = SzEbeanConfig.workerOf(ebeanServer.name)

    val ebeanFuture = CompletableFuture.supplyAsync(Supplier<T> {
        DB.initDataSourceContext()
        DB.setDataSourceContext(ebeanServer.name)

        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        ebeanServer.beginTransaction(txScope).use { transaction ->
            val result = body()
            transaction.commit()
            result
        }
    }, worker)

    return ebeanFuture.await()
}

/**
 * 该方法不能运行在协程下.
 * 提供该方法的目的, 是处理事务嵌套事务的情况
 * 该方法应该嵌套在 [suspend fun EbeanServer.runInTransaction(...)] 方法内
 */
fun runInScopedTransaction(dataSource: String? = null, readOnly: Boolean = false, body: () -> Unit) {
    try {
        if (dataSource != null) {
            DB.setDataSourceContext(dataSource)
        }

        val ebeanServer = DB.byContext()
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        ebeanServer.beginTransaction(txScope).use { transaction ->
            body()
            transaction.commit()
        }

    } finally {
        if (dataSource != null) {
            DB.unsetDataSourceContext(dataSource)
        }
    }
}

/**
 * 该方法不能运行在协程下.
 * 提供该方法的目的, 是处理事务嵌套事务的情况.
 * 该方法应该嵌套在 [suspend fun EbeanServer.runInTransaction(...)] 方法内
 */
fun <T> callInScopedTransaction(dataSource: String? = null, readOnly: Boolean = false, body: () -> T): T {
    try {
        if (dataSource != null) {
            DB.setDataSourceContext(dataSource)
        }

        val ebeanServer = DB.byContext()
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        ebeanServer.beginTransaction(txScope).use { transaction ->
            val result = body()
            transaction.commit()
            return result
        }

    } finally {
        if (dataSource != null) {
            DB.unsetDataSourceContext(dataSource)
        }
    }
}

internal object RoundRobinCounter {
    var counter = AtomicLong()
}

/**
 * 读写分离部署方式下, 针对纯查询的接口进行负载均衡
 * 1 主(写) 多 从(读), 将查询请求均衡到从数据库
 */
fun <T> queryOnly(readOnlyDataSourceList: List<String>, body: () -> T): T {
    if (readOnlyDataSourceList.isEmpty()) {
        throw RuntimeException("readOnlyDataSourceList can not be empty.")
    }
    val ebeanServer = DB.byDataSource(readOnlyDataSourceList[abs(RoundRobinCounter.counter.getAndIncrement() % readOnlyDataSourceList.size).toInt()])
    return ebeanServer.callInScopedTransaction(readOnly = true, body = body)
}

suspend fun <T> queryOnlyAwait(readOnlyDataSourceList: List<String>, body: () -> T): T {
    if (readOnlyDataSourceList.isEmpty()) {
        throw RuntimeException("readOnlyDataSourceList can not be empty.")
    }

    val ebeanServer = DB.byDataSource(readOnlyDataSourceList[abs(RoundRobinCounter.counter.getAndIncrement() % readOnlyDataSourceList.size).toInt()])
    val worker = SzEbeanConfig.workerOf(ebeanServer.name)

    val ebeanFuture = CompletableFuture.supplyAsync(Supplier<T> {
        DB.initDataSourceContext()
        val result = ebeanServer.callInScopedTransaction(readOnly = true, body = body)
        result
    }, worker)

    return ebeanFuture.await()
}

fun EbeanServer.runInScopedTransaction(readOnly: Boolean = false, body: () -> Unit) {
    try {
        DB.setDataSourceContext(this.name)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        this.beginTransaction(txScope).use { transaction ->
            body()
            transaction.commit()
        }
    } finally {
        DB.unsetDataSourceContext(this.name)
    }
}

fun <T> EbeanServer.callInScopedTransaction(readOnly: Boolean = false, body: () -> T): T {
    try {
        DB.setDataSourceContext(this.name)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        this.beginTransaction(txScope).use { transaction ->
            val result = body()
            transaction.commit()
            return result
        }
    } finally {
        DB.unsetDataSourceContext(this.name)
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <TBean : Any> SqlRow.toBean(beanClass: KClass<TBean>): TBean {
    val bean = beanClass.createInstance()
    val beanUtil = BeanUtil.pojo

    this.forEach { name, value ->
        if (beanUtil.hasProperty(bean, name)) {
            //                Logger.debug("==> name: $name, value: $value, type: ${value?.javaClass?.name}")
            val propValue = when (val propType = beanUtil.getPropertyType(bean, name)) {
                java.lang.String::class.java -> this.getString(name)
                Int::class.java, java.lang.Integer::class.java -> this.getInteger(name)
                Long::class.java, java.lang.Long::class.java -> this.getLong(name)
                Float::class.java, java.lang.Float::class.java -> this.getFloat(name)
                Double::class.java, java.lang.Double::class.java -> this.getDouble(name)
                Boolean::class.java, java.lang.Boolean::class.java -> this.getBoolean(name)
                BigDecimal::class.java -> this.getBigDecimal(name)
                JDateTime::class.java -> if (value == null) null else JDateTime(this.getUtilDate(name))
                Date::class.java -> this.getUtilDate(name)
                UUID::class.java -> this.getUUID(name)
                java.sql.Date::class.java -> this.getDate(name)
                java.sql.Timestamp::class.java -> this.getTimestamp(name)
                else -> throw BizLogicException("SqlRow.toBean() 方法不支持转换 Bean Class 中类型为 ${propType.name} 的属性, 请联系开发人员")
            }
            beanUtil.setProperty(bean, name, propValue)
        }
    }

    return bean
}

fun <TBean : Any> List<SqlRow>.toBeans(beanClass: KClass<TBean>): List<TBean> {
    return this.map { it.toBean(beanClass) }
}