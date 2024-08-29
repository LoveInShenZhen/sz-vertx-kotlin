package kts.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kts.tools.getStringOrElse

//
// Created by drago on 2024/8/7 周三.
//
class CmdVersion : CliktCommand(name = "version", help = "查看版本信息") {

    private val config: Config = ConfigFactory.load()

    override fun run() {
        println("Version: ${config.getStringOrElse ("Version", "未指定")}")
        println("Git Rev: ${config.getStringOrElse("GitRev", "未指定")}")
        println("BuildAt: ${config.getStringOrElse("BuildAt", "未指定")}")
    }
}