package sz.scaffold.controller.builtIn

import jodd.util.ClassLoaderUtil
import jodd.util.CommandLine
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
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
        val builtInLinks = if (Application.hideBuiltinPages) "" else """
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

        info.append("jvm class path list:\n")
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

        info.appendln("    Init Memory: ${initMemorySize / 1024 / 1024} MB")
        info.appendln("    Max Memory:  ${maxMemorySize / 1024 / 1024} MB")
        info.appendln("    Used Memory: ${usedMemorySize / 1024 / 1024} MB")
        info.appendln()

        val nonHeapUsage = mxBean.nonHeapMemoryUsage
        info.appendln("  NonHeap Memory Usage")

        initMemorySize = nonHeapUsage.init
        maxMemorySize = nonHeapUsage.max
        usedMemorySize = nonHeapUsage.used

        info.appendln("    Init Memory: ${initMemorySize / 1024 / 1024} MB")
        info.appendln("    Max Memory:  ${maxMemorySize / 1024 / 1024} MB")
        info.appendln("    Used Memory: ${usedMemorySize / 1024 / 1024} MB")
        info.appendln("-".repeat(64))

        info.appendln("  HostName: ${InetAddress.getLocalHost().hostName}")
        info.appendln("  HostIp: ${InetAddress.getLocalHost().hostAddress}")

        info.appendln("-".repeat(64))
        info.appendln("  Vertx Options:")
        info.appendln(Application.vertxOptions.toString())


        info.appendln("-".repeat(64))
        if (Application.vertxOptions.eventBusOptions.isClustered) {
            // 集群方式
            info.appendln("Vertx: cluster mode [当前为: Vertx 集群模式]")
            info.appendln("    Node Id: ${Application.vertxOptions.clusterManager.nodeID}")
            info.appendln("    Nodes: ${Application.vertxOptions.clusterManager.nodes.toList()}")
        } else {
            info.appendln("当前为: Vertx 单机模式")
        }

        info.appendln("-".repeat(64))
        info.appendln("application.conf:")
        info.appendln(Application.config.root().unwrapped().toJsonPretty())
        info.appendln()

        info.appendln("-".repeat(64))
        // sh -c 'ulimit -a'
        val ulimitCmd = CommandLine.cmd("sh").args("-c", "ulimit -a").outPrefix("    ")

        try {
            val cmdResult = ulimitCmd.run()
            info.appendln("ulimit info:")
            info.appendln(cmdResult.output)
        } catch (ex: Exception) {
            info.appendln("Can not get ulimit info: ${ex.message}")
        }

        return info.toString()
    }
}