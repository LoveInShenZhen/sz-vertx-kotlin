package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.LoggerFactory
import java.io.File

//
// Created by drago on 2024/8/8 周四.
//
class CmdTest : CliktCommand(name = "test", help = "仅开发时临时测试") {
    override fun run() {
        println("=".repeat(64))
        println("IDEA 调试说明")
        println("gradle 方式下传递命令行参数示例如下:")
        println("gradle run --args='-h'")
        println("gradle run --args='test'")
        println("=".repeat(64))
        println("IDEA run 方式下, 设置配置界面里的 VM options 如下")
        println("-Dprops.file=conf/ebean.yml")
        println("-Dconfig.file=conf/application.conf")
        println("-Dlogback.configurationFile=conf/logback.xml")
        println("设置工作目录为项目根目录")

    }
}