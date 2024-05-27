package myquant

import com.github.ajalt.clikt.core.subcommands
import myquant.cmds.*
import org.slf4j.LoggerFactory
import java.io.File

//
// Created by kk on 2022/5/29.
//
class MainApp {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            initLogbackConfigFilePath()
            CmdRoot()
                .subcommands(CmdTest())
                .main(args)
        }

        private fun initLogbackConfigFilePath() {
            val logbackConfFile = System.getProperty("logback.configurationFile", "")
            if (logbackConfFile != "") {
                val log = LoggerFactory.getLogger("app")
                log.info("logback.configurationFile: ${logbackConfFile}")

                log.info("working dir: ${File(".").absolutePath.trim('.')}")
                log.info("config.file: ${System.getProperty("config.file", "undefined")}")
                log.info("ebean props.file: ${System.getProperty("props.file", "undefined")}")
            }
        }

    }
}