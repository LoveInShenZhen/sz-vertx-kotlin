package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//
// Created by drago on 2022/12/22 022.
//
class CmdTest : CliktCommand(help = "临时测试代码", name = "test") {
    val log: Logger = LoggerFactory.getLogger("app")



    lateinit var config: Config

    override fun run() {
        initConfig()

    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }

}