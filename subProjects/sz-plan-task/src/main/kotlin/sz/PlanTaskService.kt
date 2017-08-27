package sz

import io.vertx.core.eventbus.MessageConsumer
import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import models.PlanTask
import models.TaskStatus
import sz.scaffold.Application
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//
// Created by kk on 17/8/25.
//
object PlanTaskService {

    private val parallelWorkerCount: Int
        get() = Application.config.getIntOrElse("app.PlanTask.parallelWorkerCount", Runtime.getRuntime().availableProcessors())

    private val seqPlanningWorker: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val parallelPlanningWorker: ScheduledExecutorService = Executors.newScheduledThreadPool(parallelWorkerCount)

    private var seqPlanningTaskLoader: Thread? = null
    private var parallerPlanningTaskLoader: Thread? = null

    private val taskNotifier = Object()
    private val taskLoaderWaitTime = 60
    private var stopNow: Boolean = true

    val eventBusAddress = "sz.app.plantask.newtask"
    private var eventConsumer: MessageConsumer<Any>? = null

    var isRunning: Boolean = false
        private set

    fun Start() {
        try {
            if (!enabled()) {
                Logger.debug("PlanTaskServer 不能允许. 请检查配置和数据库是否就绪")
                return
            }

            if (isRunning) return

            stopNow = false

            seqPlanningTaskLoader = BuildPlanningTaskLoader(true, seqPlanningWorker)
            parallerPlanningTaskLoader = BuildPlanningTaskLoader(false, parallelPlanningWorker)

            seqPlanningTaskLoader!!.start()
            parallerPlanningTaskLoader!!.start()

            isRunning = true

            PlanTask.ResetTaskStatus()

            eventConsumer = Application.vertx.eventBus().consumer(eventBusAddress) { _ -> notifyNewTask() }

            Logger.info("Plan Task Service Started......", AnsiColor.GREEN)

        } catch (ex: Exception) {
            stopNow = true
            isRunning = false

            Logger.warn("Start planTask service failed.\n${ExceptionUtil.exceptionStackTraceToString(ex)}", AnsiColor.RED_B)
        }
    }

    fun Stop() {
        Logger.info("Try to stop paln task service ...")
        if (!isRunning) return
        stopNow = true
        try {
            Logger.debug("Try to stop plan task loader...")
            notifyNewTask()
            seqPlanningTaskLoader!!.join(120000)
            parallerPlanningTaskLoader!!.join(120000)

            Logger.debug("Try to stop plan task worker...")
            seqPlanningWorker.shutdown()
            parallelPlanningWorker.shutdown()

            seqPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)
            parallelPlanningWorker.awaitTermination(120, TimeUnit.SECONDS)

            eventConsumer!!.unregister()

            Logger.debug("Plan Task Service Stopped......")
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
        } finally {
            isRunning = false
        }
    }

    private fun BuildPlanningTaskLoader(requireSeq: Boolean, worker: ScheduledExecutorService): Thread {
        return Thread(Runnable {
            val loadedTasks = mutableListOf<PlanTask>()
            while (true) {
                if (stopNow) break

                try {
                    DB.Default().RunInTransaction {
                        val endTime = JDateTime().addSecond(taskLoaderWaitTime + 1)
                        val tasks = PlanTask.where()
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
                        DB.Default().saveAll(tasks)

                        loadedTasks.clear()
                        loadedTasks.addAll(tasks)
                    }   // 事务截至点

                    if (loadedTasks.size > 0) {
                        loadedTasks.forEach {
                            SchedulePlanTask(it, worker)
                        }
                    } else {
                        // 在 endTime 之前没有需要执行的 task, 尝试等待新任务, 释放 cpu
                        try {
                            synchronized(taskNotifier, {
                                Logger.debug("开始等待task requireSeq:[$requireSeq}] 最多: $taskLoaderWaitTime 秒")
                                taskNotifier.wait(taskLoaderWaitTime * 1000L)
                            })
                        } catch (ex: Exception) {
                            // do nothing
                        }
                    }

                } catch (ex: Exception) {
                    loadedTasks.clear()
                    Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
                    Thread.sleep(60 * 1000)
                }
            }
            Logger.debug("Stop PlanningTaskLoader for requireSeq: $requireSeq")
        })
    }

    private fun SchedulePlanTask(task: PlanTask, worker: ScheduledExecutorService) {
        val now = JDateTime()
        if (task.plan_run_time == null) {
            // plan_run_time 为 null 表示立即执行
            worker.submit {
                try {
                    process_task(task)
                } catch (ex: Exception) {
                    Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
                }
            }

            return
        }
        val interval = task.plan_run_time!!.timeInMillis - now.timeInMillis
        val delay = if (interval > 0) interval else 0

        worker.schedule({
            try {
                process_task(task)
            } catch (ex: Exception) {
                Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
            }
        },
                delay,
                TimeUnit.MILLISECONDS)
    }

    private fun DeserializeJsonData(task: PlanTask): Runnable? {
        try {
            return Json.fromJsonString(task.json_data!!, Class.forName(task.class_name)) as Runnable
        } catch (ex: Exception) {
            return null
        }
    }

    private fun process_task(task: PlanTask) {
        try {
            val runObj = DeserializeJsonData(task)
            if (runObj != null) {
                try {
                    DB.Default().RunInTransaction({
                        runObj.run()    // 执行任务
                        task.refresh()
                        task.delete()   // 任务执行成功后, 从数据库里删除记录
                    })
                } catch (ex: Exception) {
                    // 任务执行发生错误, 标记任务状态, 记录
                    DB.Default().RunInTransaction {
                        task.refresh()
                        task.task_status = TaskStatus.Error.code
                        task.remarks = ExceptionUtil.exceptionStackTraceToString(ex)
                        task.save()
                    }
                }
            } else {
                DB.Default().RunInTransaction {
                    task.refresh()
                    task.task_status = TaskStatus.Error.code
                    task.remarks = "反序列化任务失败"
                    task.save()
                }
            }
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionStackTraceToString(ex))
        }
    }

    private fun notifyNewTask() {
        synchronized(taskNotifier, {
            taskNotifier.notifyAll()
        })
    }

    fun enabled(): Boolean {
        return Application.config.hasPath("service.planTask")
                && Application.config.getBoolean("service.planTask")
                && DB.Default().TableExists("plan_task")
    }

}