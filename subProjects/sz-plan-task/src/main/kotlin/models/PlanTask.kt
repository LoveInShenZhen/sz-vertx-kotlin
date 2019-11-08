package models

import io.ebean.EbeanServer
import io.ebean.Finder
import io.ebean.Model
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jodd.datetime.JDateTime
import sz.PlanTaskService
import sz.annotations.DBIndexed
import sz.ebean.DB
import sz.ebean.runTransactionBlocking
import sz.scaffold.Application
import sz.scaffold.ext.getStringOrElse
import sz.scaffold.tools.json.toJsonPretty
import java.sql.Timestamp
import javax.persistence.*

//
// Created by kk on 17/8/25.
//
@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
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

    @Column(columnDefinition = "TINYINT(1) COMMENT '是否要求顺序执行'")
    var require_seq: Boolean = false

    @Column(columnDefinition = "VARCHAR(64) COMMENT '顺序执行的类别'", nullable = false)
    var seq_type: String? = null

    @DBIndexed
    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'")
    var plan_run_time: JDateTime? = null

    @Column(columnDefinition = "INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Error'", nullable = false)
    var task_status: Int = 0

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Runnable task class name'", nullable = false)
    var class_name: String? = null

    @Column(columnDefinition = "TEXT COMMENT 'Runnable task class json data'", nullable = false)
    var json_data: String? = null

    @Column(columnDefinition = "TEXT COMMENT '标签,用于保存任务相关的额外数据'")
    var tag: String? = null

    @Column(columnDefinition = "TEXT COMMENT '发生异常情况的时候, 用于记录额外信息'")
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

        fun addTask(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionBlocking {
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

            notifyNewTask()
        }

        fun addSingletonTask(task: Runnable, requireSeq: Boolean = false, seqType: String = "", planRunTime: JDateTime? = null, tag: String = "") {
            taskDB.runTransactionBlocking { ebeanServer ->
                val className = task.javaClass.name
                val oldTasks = finder().query().where()
                    .eq("class_name", className)
                    .`in`("task_status", TaskStatus.WaitingInDB.code, TaskStatus.WaitingInQueue.code)
                    .findList()

                // 先删除在数据库等待的旧任务
                ebeanServer.deleteAll(oldTasks)
                // 添加新版任务
                addTask(task, requireSeq, seqType, planRunTime, tag)

            }

            notifyNewTask()
        }

        fun resetTaskStatus() {
            taskDB.runTransactionBlocking { ebeanServer ->
                val sql = "update `plan_task` set `task_status`=:init_status where `task_status`=:old_status"
                ebeanServer.createSqlUpdate(sql)
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