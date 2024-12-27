package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//
// Created by kk on 2022/5/28.
//
class CmdRoot : CliktCommand(name = "fix-db", help = "管理中心升级v5.9版本前,对数据库表结构进行检查和修正") {

    val log: Logger = LoggerFactory.getLogger("app")

    val config: Config
        get() {
            return ConfigFactory.load()
        }

    override fun run() {
    }
}

