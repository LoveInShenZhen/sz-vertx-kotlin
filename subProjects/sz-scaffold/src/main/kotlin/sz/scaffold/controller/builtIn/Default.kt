package sz.scaffold.controller.builtIn

import jodd.util.ClassLoaderUtil
import jodd.util.CommandLine
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.aop.interceptors.builtin.api.DevModeOnly
import sz.scaffold.controller.ApiController
import sz.scaffold.controller.ContentTypes
import sz.scaffold.tools.json.toJsonPretty
import java.lang.management.ManagementFactory
import java.net.InetAddress

//
// Created by kk on 17/9/8.
//

@Comment("内置默认的控制器内,提供一些关于系统信息的查询功能")
class Default : ApiController() {

    fun hello(): String {
        this.contentType(ContentTypes.Html)
        val builtInLinks = if (Application.inProductionMode) "" else """
            <ul>
                <li><a href="/api/builtin/doc/apiIndex">api 列表</a></li>
                <li><a href="/api/builtin/doc/pageIndex">非 api 链接列表</a></li>
                <li><a href="/api/builtin/sysInfo">系统信息</a></li>
                <li><a href="/api/builtin/doc/apiDocMarkdown">api 文档的markdown格式</a></li>
                <li><a href="/api/builtin/doc/apiDocHtml">api 文档的html格式</a></li>
            </ul>
        """.trimIndent()

        return """
<html>
<head>
<style type="text/css">
    body {
        background: #e3dede;
    }
</style>
</head>
<body>
    <p>欢迎使用 SZ 后端快速开发框架 <a href="https://loveinshenzhen.github.io/#/sz_framework/introduction">文档请看</a></p>
$builtInLinks
</body>
</html>""".trimIndent()
    }

    @Comment("返回系统信息")
    @DevModeOnly
    fun sysInfo(): String {
        val info = StringBuilder()

        info.append("system environment:\n")
        info.appendLine("-".repeat(64))

        System.getenv().forEach { k, v ->
            info.appendLine("  $k : $v")
        }
        info.appendLine("-".repeat(64))
        info.appendLine()

        info.appendLine("system properties:")
        info.appendLine("-".repeat(64))

        System.getProperties().forEach { k, v ->
            info.appendLine("  $k : $v")
        }
        info.appendLine("-".repeat(64))
        info.appendLine()

        info.append("jvm class path list:\n")
        info.appendLine("-".repeat(64))
        ClassLoaderUtil.getDefaultClasspath().forEach { file ->
            info.appendLine("  ${file.absolutePath}")
        }
        info.appendLine("-".repeat(64))

        info.appendLine("Memory Managemen info:")

        val mxBean = ManagementFactory.getMemoryMXBean()
        val heapUsage = mxBean.heapMemoryUsage
        info.appendLine("  Heap Memory Usage")

        var initMemorySize = heapUsage.init
        var maxMemorySize = heapUsage.max
        var usedMemorySize = heapUsage.used

        info.appendLine("    Init Memory: ${initMemorySize / 1024 / 1024} MB")
        info.appendLine("    Max Memory:  ${maxMemorySize / 1024 / 1024} MB")
        info.appendLine("    Used Memory: ${usedMemorySize / 1024 / 1024} MB")
        info.appendLine()

        val nonHeapUsage = mxBean.nonHeapMemoryUsage
        info.appendLine("  NonHeap Memory Usage")

        initMemorySize = nonHeapUsage.init
        maxMemorySize = nonHeapUsage.max
        usedMemorySize = nonHeapUsage.used

        info.appendLine("    Init Memory: ${initMemorySize / 1024 / 1024} MB")
        info.appendLine("    Max Memory:  ${maxMemorySize / 1024 / 1024} MB")
        info.appendLine("    Used Memory: ${usedMemorySize / 1024 / 1024} MB")
        info.appendLine("-".repeat(64))

        info.appendLine("  HostName: ${InetAddress.getLocalHost().hostName}")
        info.appendLine("  HostIp: ${InetAddress.getLocalHost().hostAddress}")

        info.appendLine("-".repeat(64))
        info.appendLine("  Vertx Options:")
        info.appendLine(Application.vertxOptions.toString())


        info.appendLine("-".repeat(64))
        if (Application.vertxOptions.eventBusOptions.isClustered) {
            // 集群方式
            info.appendLine("Vertx: cluster mode [当前为: Vertx 集群模式]")
            info.appendLine("    Node Id: ${Application.vertxOptions.clusterManager.nodeID}")
            info.appendLine("    Nodes: ${Application.vertxOptions.clusterManager.nodes.toList()}")
        } else {
            info.appendLine("当前为: Vertx 单机模式")
        }

        info.appendLine("-".repeat(64))
        info.appendLine("application.conf:")
        info.appendLine(Application.config.root().unwrapped().toJsonPretty())
        info.appendLine()

        info.appendLine("-".repeat(64))
        // sh -c 'ulimit -a'
        val ulimitCmd = CommandLine.cmd("sh").args("-c", "ulimit -a").outPrefix("    ")

        try {
            val cmdResult = ulimitCmd.run()
            info.appendLine("ulimit info:")
            info.appendLine(cmdResult.output)
        } catch (ex: Exception) {
            info.appendLine("Can not get ulimit info: ${ex.message}")
        }

        return info.toString()
    }
}