package sz.scaffold.controller.builtIn

import jodd.util.ClassLoaderUtil
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import java.lang.management.ManagementFactory
import java.net.InetAddress

//
// Created by kk on 17/9/8.
//
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
        var freeMemorySize = initMemorySize - usedMemorySize

        info.appendln("    Init Memory: ${initMemorySize/1024/1024} MB")
        info.appendln("    Max Memory:  ${maxMemorySize/1024/1024} MB")
        info.appendln("    Used Memory: ${usedMemorySize/1024/1024} MB")
        info.appendln("    Free Memory: ${freeMemorySize/1024/1024} MB")
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

        return info.toString()
    }
}