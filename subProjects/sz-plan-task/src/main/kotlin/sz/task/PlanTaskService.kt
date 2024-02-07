package sz.task

import io.ebean.Database as EbeanServer
import io.vertx.core.eventbus.MessageConsumer
import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import models.PlanTask
import models.TaskStatus
import sz.ebean.DB
import sz.ebean.SzEbeanConfig
import sz.ebean.runTransactionBlocking
import sz.logger.log
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.tools.json.Json
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//
// Created by kk on 17/8/25.
//
@Suppress("MemberVisibilityCanBePrivate", "FunctionName")
object PlanTaskService {

    private val parallelWorkerCount: Int
        get() = Application.config.getIntOrElse("app.PlanTask.parallelWorkerCount", Runtime.getRuntime().availableProcessors())

    private val seqPlanningWorker: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val parallelPlanningWorker: ScheduledExecutorService = Executors.newScheduledThreadPool(parallelWorkerCount)

    private var seqPlanningTaskLoader: Thread? = null
    private var parallerPlanningTaskLoader: Thread? = null

    private val taskNotifier = Object()
    private const val taskLoaderWaitTime = 60
    private var stopNow: Boolean = true

    const val eventBusAddress = "sz.app.plantask.newtask"
    private var eventConsumer: MessageConsumer<Any>? = null

    var isRunning: Boolean = false
        private set

    fun start() {
        try {
            if (enabled.not()) {
                log.debug("PlanTaskServer 不能运行. 请检查配置和数据库是否就绪")
                return
            }

            if (isRunning) return

            stopNow = false

            waitFroTaskDbReady()

            seqPlanningTaskLoader = buildPlanningTaskLoader(true, seqPlanningWorker)
            parallerPlanningTaskLoader = buildPlanningTaskLoader(false, parallelPlanningWorker)

            seqPlanningTaskLoader!!.start()
            parallerPlanningTaskLoader!!.start()

            isRunning = true

            PlanTask.resetTaskStatus()

            eventConsumer = Application.vertx.eventBus().consumer(eventBusAddress) { _ -> notifyNewTask() }

            log.info("Plan Task Service Started......")

        } catch (ex: Exception) {
            stopNow = true
            isRunning = false

            log.warn("Start planTask service failed.\n${ExceptionUtil.exceptionStackTraceToString(ex)}")
        }
    }

    fun stop() {
        log.info("Try to stop paln task service ...")
        if (!isRunning) return
        stopNow = true
        try {
            log.debug("Try to stop plan task loader...")
            notifyNewTask()
            seqPlanningTaskLoader!!.join(120000)
            parallerPlanningTaskLoader!!.join(120000)

            log.debug("Try to stop plan task worker...")
            seqPlanningWorker.shutdown()
            parallelPlanningWorker.shutdown()

            seqPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)
            parallelPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)

            eventConsumer!!.unregister()

            log.debug("Plan Task Service Stopped......")
        } catch (ex: Exception) {
            log.error(ExceptionUtil.exceptionStackTraceToString(ex))
        } finally {
            isRunning = false
        }
    }

    /**
     * 等待 PlanTask 的数据库连接就绪
     */
    private fun waitFroTaskDbReady() {
        while (true) {
            if (PlanTask.dataSourceName.isBlank()) {
                // PlanTask 使用的是默认数据源
                if (SzEbeanConfig.defaultDatasourceReady) {
                    // 默认数据源已经就绪
                    return
                } else {
                    // 等待 1 秒后重新检测数据源是否就绪
                    Thread.sleep(1000)
                    continue
                }
            } else if (SzEbeanConfig.ebeanServerConfigs.containsKey(PlanTask.dataSourceName)) {
                // PlanTask 使用的是指定的数据源, 并且已经就绪
                return
            } else {
                // 等待 1 秒后重新检测数据源是否就绪
                Thread.sleep(1000)
                continue
            }
        }
    }

    private fun buildPlanningTaskLoader(requireSeq: Boolean, worker: ScheduledExecutorService): Thread {
        return Thread(Runnable {
            val loadedTasks = mutableListOf<PlanTask>()
            while (true) {
                if (stopNow) break

                try {
                    taskDB.runTransactionBlocking {
                        val endTime = JDateTime().addSecond(taskLoaderWaitTime + 1)
                        val tasks = PlanTask.finder().query().where()
                            .eq("require_seq", requireSeq)
                            .eq("task_status", TaskStatus.WaitingInDB.code)
                            .or()
                            .le("plan_run_time", endTime)
                            .isNull("plan_run_time")
                            .endOr()
                            .findList()

                        tasks.forEach {
                            it.task_status = TaskStatus.WaitingInQueue.code
                        }
                        taskDB.saveAll(tasks)

                        loadedTasks.clear()
                        loadedTasks.addAll(tasks)
                    }   // 事务截至点

                    if (loadedTasks.size > 0) {
                        loadedTasks.forEach {
                            schedulePlanTask(it, worker)
                        }
                    } else {
                        // 在 endTime 之前没有需要执行的 task, 尝试等待新任务, 释放 cpu
                        try {
                            synchronized(taskNotifier) {
                                //  log.debug("开始等待task requireSeq:[$requireSeq}] 最多: $taskLoaderWaitTime 秒")
                                taskNotifier.wait(taskLoaderWaitTime * 1000L)
                            }
                        } catch (ex: Exception) {
                            // do nothing
                        }
                    }

                } catch (ex: Exception) {
                    loadedTasks.clear()
                    log.error(ExceptionUtil.exceptionStackTraceToString(ex))
                    Thread.sleep(10 * 1000L)
                }
            }
            log.debug("Stop PlanningTaskLoader for requireSeq: $requireSeq")
        })
    }

    private fun schedulePlanTask(task: PlanTask, worker: ScheduledExecutorService) {
        val now = JDateTime()
        if (task.plan_run_time == null) {
            // plan_run_time 为 null 表示立即执行
            worker.submit {
                try {
                    processTask(task)
                } catch (ex: Exception) {
                    log.error(ExceptionUtil.exceptionStackTraceToString(ex))
                }
            }

            return
        }
        val interval = task.plan_run_time!!.timeInMillis - now.timeInMillis
        val delay = if (interval > 0) interval else 0

        worker.schedule({
            try {
                processTask(task)
            } catch (ex: Exception) {
                log.error(ExceptionUtil.exceptionStackTraceToString(ex))
            }
        },
            delay,
            TimeUnit.MILLISECONDS)
    }

    private fun deserializeJsonData(task: PlanTask): Runnable? {
        try {
            return Json.fromJsonString(task.json_data!!, Class.forName(task.class_name)) as Runnable
        } catch (ex: Exception) {
            return null
        }
    }

    private fun processTask(task: PlanTask) {
        try {
            val runObj = deserializeJsonData(task)
            if (runObj != null) {
                try {
                    taskDB.runTransactionBlocking {
                        runObj.run()    // 执行任务
                        val originTask = PlanTask.finder().query().where().idEq(task.id).findOneOrEmpty()
                        originTask.ifPresent { theTask ->
                            theTask.delete()
                        }
                    }
                } catch (ex: Exception) {
                    // 任务执行发生错误, 标记任务状态, 记录
                    taskDB.runTransactionBlocking {
                        val originTask = PlanTask.finder().query().where().idEq(task.id).findOneOrEmpty()
                        originTask.ifPresent { theTask ->
                            theTask.task_status = TaskStatus.Error.code
                            theTask.remarks = ExceptionUtil.exceptionStackTraceToString(ex)
                            theTask.save()
                        }
                    }
                }
            } else {
                taskDB.runTransactionBlocking {
                    val originTask = PlanTask.finder().query().where().idEq(task.id).findOneOrEmpty()
                    originTask.ifPresent { theTask ->
                        theTask.task_status = TaskStatus.Error.code
                        theTask.remarks = "反序列化任务失败"
                        theTask.save()
                    }
                }
            }
        } catch (ex: Exception) {
            log.error(ExceptionUtil.exceptionStackTraceToString(ex))
        }
    }

    private fun notifyNewTask() {
        synchronized(taskNotifier) {
            taskNotifier.notifyAll()
        }
    }

    val taskDB: EbeanServer
        get() {
            return DB.byDataSource(PlanTask.dataSourceName)
        }

    val enabled: Boolean by lazy {
        Application.config.hasPath("service.planTask.enable")
            && Application.config.getBoolean("service.planTask.enable")
    }

}