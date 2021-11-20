package models

import io.ebean.Finder
import io.ebean.Model
import io.ebean.annotation.*
import io.ebean.annotation.Index
import jodd.datetime.JDateTime
import sz.ebean.DB
import sz.ebean.runTransactionAwait
import sz.ebean.runTransactionBlocking
import sz.scaffold.Application
import sz.scaffold.ext.getStringOrElse
import sz.scaffold.tools.json.toJsonPretty
import sz.task.PlanTaskService
import java.sql.Timestamp
import javax.persistence.*
import io.ebean.Database as EbeanServer

//
// Created by kk on 17/8/25.
//
@Suppress("MemberVisibilityCanBePrivate", "PropertyName", "DuplicatedCode")
@DbComment("计划任务")
@Entity
@Table(name = "plan_task")
class PlanTask(dataSource: String = dataSourceName) : Model(dataSource) {

    @Id
    var id: Long = 0

    @Version
    var version: Long? = null

    @WhenCreated
    var whenCreated: Timestamp? = null

    @WhenModified
    var whenModified: Timestamp? = null

    @DbComment("是否要求顺序执行")
    @Column
    var require_seq: Boolean = false

    @DbComment("顺序执行的类别")
    @DbDefault("")
    @Column(length = 64, nullable = false)
    var seq_type: String? = null

    @Index
    @DbComment("任务计划执行时间")
    @Column()
    var plan_run_time: JDateTime? = null

    @DbComment("任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Error")
    @DbDefault("0")
    @Column(nullable = false)
    var task_status: Int = 0

    @DbComment("Runnable task class name")
    @Column(length = 1024, nullable = false)
    var class_name: String? = null

    @DbComment("Runnable task class json data")
    @Lob
    @Column(nullable = false)
    var json_data: String? = null

    @DbComment("标签,用于保存任务相关的额外数据")
    @DbDefault("")
    @Column(length = 1024)
    var tag: String? = null

    @DbComment("发生异常情况的时候, 用于记录额外信息")
    @Lob
    @DbDefault("")
    @Column
    var remarks: String? = null

    companion object {

        val dataSourceName: String by lazy {
            Application.config.getStringOrElse("service.planTask.dataSource", "")
        }

        fun finder(): Finder<Long, PlanTask> {
            return DB.finder(dataSourceName)
        }

        val taskDB: EbeanServer by lazy {
            DB.byDataSource(dataSourceName)
        }

        private fun newTask(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            val planTask = PlanTask()
            planTask.require_seq = requireSeq
            planTask.seq_type = seqType
            planTask.plan_run_time = planRunTime
            planTask.task_status = TaskStatus.WaitingInDB.code
            planTask.class_name = task.javaClass.name
            planTask.json_data = task.toJsonPretty()
            planTask.tag = tag

            planTask.save()
        }

        fun addTaskBlocking(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionBlocking {
                newTask(task, requireSeq, seqType, planRunTime, tag)
            }
            notifyNewTask()
        }

        suspend fun addTaskAwait(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionAwait {
                newTask(task, requireSeq, seqType, planRunTime, tag)
            }
            notifyNewTask()
        }

        fun addSingletonTaskBlocking(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionBlocking { ebeanServer ->
                val className = task.javaClass.name
                val oldTasks = finder().query().where()
                    .eq("class_name", className)
                    .`in`("task_status", TaskStatus.WaitingInDB.code, TaskStatus.WaitingInQueue.code)
                    .findList()

                // 先删除在数据库等待的旧任务
                ebeanServer.deleteAll(oldTasks)

                // 添加新版任务
                newTask(task, requireSeq, seqType, planRunTime, tag)
            }

            notifyNewTask()
        }

        suspend fun addSingletonTaskAwait(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionAwait { ebeanServer ->
                val className = task.javaClass.name
                val oldTasks = finder().query().where()
                    .eq("class_name", className)
                    .`in`("task_status", TaskStatus.WaitingInDB.code, TaskStatus.WaitingInQueue.code)
                    .findList()

                // 先删除在数据库等待的旧任务
                ebeanServer.deleteAll(oldTasks)

                // 添加新版任务
                newTask(task, requireSeq, seqType, planRunTime, tag)
            }

            notifyNewTask()
        }

        fun resetTaskStatus() {
            taskDB.runTransactionBlocking { ebeanServer ->
                val sql = "update `plan_task` set `task_status`=:init_status where `task_status`=:old_status"
                ebeanServer.sqlUpdate(sql)
                    .setParameter("init_status", TaskStatus.WaitingInDB.code)
                    .setParameter("old_status", TaskStatus.WaitingInQueue.code)
                    .execute()
            }
        }

        fun notifyNewTask() {
            Application.vertx.eventBus().publish(PlanTaskService.eventBusAddress, "")
        }

    }
}

enum class TaskStatus(val desc: String, val code: Int) {
    WaitingInDB("WaitingInDB", 0),
    WaitingInQueue("WaitingInQueue", 7),
    Error("Error", 8)
}