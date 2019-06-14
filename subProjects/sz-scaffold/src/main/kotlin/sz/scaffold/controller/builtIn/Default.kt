package sz.scaffold.controller.builtIn

import jodd.util.ClassLoaderUtil
import jodd.util.CommandLine
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import java.lang.management.ManagementFactory
import java.net.InetAddress

//
// Created by kk on 17/9/8.
//

@Comment("内置默认的控制器内,提供一些关于系统信息的查询功能")
class Default : ApiController() {

    fun hello() : String {
        this.contentType("text/plain")
        return "hello"
    }

    @Comment("返回系统信息")
    fun sysInfo(): String {
        val info = StringBuilder()

        info.append("system environment:\n")
        info.appendln("-".repeat(64))

        System.getenv().forEach { k, v ->
            info.appendln("  $k : $v")
        }
        info.appendln("-".repeat(64))
        info.appendln()

        info.appendln("system properties:")
        info.appendln("-".repeat(64))

        System.getProperties().forEach { k, v ->
            info.appendln("  $k : $v")
        }
        info.appendln("-".repeat(64))
        info.appendln()

        info.append("jvm cleas path list:\n")
        info.appendln("-".repeat(64))
        ClassLoaderUtil.getDefaultClasspath().forEach { file ->
            info.appendln("  ${file.absolutePath}")
        }
        info.appendln("-".repeat(64))

        info.appendln("Memory Managemen info:")

        val mxBean = ManagementFactory.getMemoryMXBean()
        val heapUsage = mxBean.heapMemoryUsage
        info.appendln("  Heap Memory Usage")

        var initMemorySize = heapUsage.init
        var maxMemorySize = heapUsage.max
        var usedMemorySize = heapUsage.used

        info.appendln("    Init Memory: ${initMemorySize/1024/1024} MB")
        info.appendln("    Max Memory:  ${maxMemorySize/1024/1024} MB")
        info.appendln("    Used Memory: ${usedMemorySize/1024/1024} MB")
        info.appendln()

        val nonHeapUsage = mxBean.nonHeapMemoryUsage
        info.appendln("  NonHeap Memory Usage")

        initMemorySize = nonHeapUsage.init
        maxMemorySize = nonHeapUsage.max
        usedMemorySize = nonHeapUsage.used

        info.appendln("    Init Memory: ${initMemorySize/1024/1024} MB")
        info.appendln("    Max Memory:  ${maxMemorySize/1024/1024} MB")
        info.appendln("    Used Memory: ${usedMemorySize/1024/1024} MB")
        info.appendln("-".repeat(64))

        info.appendln("  HostName: ${InetAddress.getLocalHost().hostName}")
        info.appendln("  HostIp: ${InetAddress.getLocalHost().hostAddress}")

        info.appendln("-".repeat(64))
        info.appendln("  Vertx Options:")
        info.appendln(Application.vertxOptions.toString())



        if (Application.vertxOptions.eventBusOptions.isClustered) {
            info.appendln("-".repeat(64))
            // 集群方式
            info.appendln("    Node Id: ${Application.vertxOptions.clusterManager.nodeID}")
            info.appendln("    Nodes: ${Application.vertxOptions.clusterManager.nodes.toList()}")
        }

        info.appendln("-".repeat(64))

        val ulimitCmd = CommandLine.cmd("ulimit").args("-a").outPrefix("    ")

        try {
            val cmdResult = ulimitCmd.run()
            info.appendln("ulimit info:")
            info.appendln(cmdResult.output)
        } catch (ex:Exception) {
            info.appendln("Can not get ulimit info: ${ex.message}")
        }

        return info.toString()
    }
}