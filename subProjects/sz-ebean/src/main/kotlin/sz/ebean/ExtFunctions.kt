@file:Suppress("DuplicatedCode")

package sz.ebean

import io.ebean.EbeanServer
import io.ebean.SqlRow
import io.ebean.TxScope
import io.ebean.annotation.TxIsolation
import jodd.bean.BeanUtil
import jodd.datetime.JDateTime
import kotlinx.coroutines.future.await
import sz.scaffold.ext.camelCaseToLowCaseSeprated
import sz.scaffold.tools.BizLogicException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier
import javax.persistence.Entity
import javax.persistence.Table
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
    val count = rows.count { it.values.first().toString().equals(tableName, true) }
    return count > 0
}

suspend fun <T> EbeanServer.runTransactionAwait(readOnly: Boolean = false, body: (ebeanServer: EbeanServer) -> T): T {
    val ebeanServer = this
    val worker = SzEbeanConfig.workerOf(ebeanServer.name)

    val ebeanFuture = CompletableFuture.supplyAsync(Supplier<T> {
        try {
            DB.initDataSourceContext()
            DB.setDataSourceContext(ebeanServer.name)

            val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
            ebeanServer.beginTransaction(txScope).use { transaction ->
                val result = body(ebeanServer)
                transaction.commit()
                result
            }
        } finally {
            DB.unsetDataSourceContext()
        }

    }, worker)

    return ebeanFuture.await()
}

internal object RoundRobinCounter {
    var counter = AtomicLong()
}

/**
 * 读写分离部署方式下, 针对纯查询的接口进行负载均衡
 * 1 主(写) 多 从(读), 将查询请求均衡到从数据库
 * 在当前线程上执行JDBC, 所以会阻塞当前线程. 不要在协程方法(suspend)里调用此方法
 * 如果在协程方法里, 请使用 [queryOnlyAwait]
 *
 */
fun <T> queryOnlyBlocking(readOnlyDataSourceList: List<String>, body: (ebeanServer: EbeanServer) -> T): T {
    if (readOnlyDataSourceList.isEmpty()) {
        throw RuntimeException("readOnlyDataSourceList can not be empty.")
    }
    val ebeanServer = DB.byDataSource(readOnlyDataSourceList[abs(RoundRobinCounter.counter.getAndIncrement() % readOnlyDataSourceList.size).toInt()])
    return ebeanServer.runTransactionBlocking(readOnly = true, body = body)
}

suspend fun <T> queryOnlyAwait(readOnlyDataSourceList: List<String>, body: (ebeanServer: EbeanServer) -> T): T {
    if (readOnlyDataSourceList.isEmpty()) {
        throw RuntimeException("readOnlyDataSourceList can not be empty.")
    }

    val ebeanServer = DB.byDataSource(readOnlyDataSourceList[abs(RoundRobinCounter.counter.getAndIncrement() % readOnlyDataSourceList.size).toInt()])
    val worker = SzEbeanConfig.workerOf(ebeanServer.name)

    val ebeanFuture = CompletableFuture.supplyAsync(Supplier<T> {
        try {
            DB.initDataSourceContext()
            DB.setDataSourceContext(ebeanServer.name)

            val result = ebeanServer.runTransactionBlocking(readOnly = true, body = body)
            result
        } finally {
            DB.unsetDataSourceContext()
        }

    }, worker)

    return ebeanFuture.await()
}

/**
 * 在当前线程上执行JDBC, 所以会阻塞当前线程. 不要在协程方法(suspend)里调用此方法
 * 如果在协程方法里, 请使用 [EbeanServer.runTransactionAwait]
 */
fun <T> EbeanServer.runTransactionBlocking(readOnly: Boolean = false, body: (ebeanServer: EbeanServer) -> T): T {
    try {
        DB.setDataSourceContext(this.name)
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED).setReadOnly(readOnly)
        this.beginTransaction(txScope).use { transaction ->
            val result = body(this)
            transaction.commit()
            return result
        }
    } finally {
        DB.unsetDataSourceContext()
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

fun isEntityClass(modelClass: Class<*>): Boolean {
    val annoEntity = modelClass.getAnnotation(Entity::class.java)
    return annoEntity != null
}

fun getTableName(modelClass: Class<*>): String {
    modelClass.getAnnotation(Entity::class.java) ?: throw BizLogicException("不是实体类")

    val annoTable = modelClass.getAnnotation(Table::class.java)
    if (annoTable != null && annoTable.name.isNotBlank()) {
        return annoTable.name
    } else {
        return modelClass.simpleName.camelCaseToLowCaseSeprated('_')
    }

}