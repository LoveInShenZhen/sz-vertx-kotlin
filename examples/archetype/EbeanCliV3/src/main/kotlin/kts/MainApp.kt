package kts

import com.github.ajalt.clikt.core.subcommands
import kts.cmds.*
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
                .subcommands(
                    CmdTest(),
                    CmdVersion(),
                )
                .main(args)
        }

        private fun initLogbackConfigFilePath() {
            val logbackConfFile = System.getProperty("logback.configurationFile", "")
            if (logbackConfFile != "") {
                val log = LoggerFactory.getLogger("app")
                log.debug("logback.configurationFile: ${logbackConfFile}")

                log.debug("working dir: ${File(".").absolutePath.trim('.')}")
                log.debug("config.file: ${System.getProperty("config.file", "undefined")}")
                log.debug("ebean props.file: ${System.getProperty("props.file", "undefined")}")
            }
        }

    }
}