package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//
// Created by kk on 2022/5/28.
//
class CmdRoot : CliktCommand(name = "data_checker") {

    val log: Logger = LoggerFactory.getLogger("app")

    val config: Config
        get() {
            return ConfigFactory.load()
        }

    override fun run() {
    }
}