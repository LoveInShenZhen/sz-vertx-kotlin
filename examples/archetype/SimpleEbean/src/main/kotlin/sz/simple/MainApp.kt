package sz.simple

import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import org.slf4j.LoggerFactory
import sz.simple.cmds.CmdRoot

//
// Created by kk on 2022/5/29.
//
class MainApp {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            initLogbackConfigFilePath()
            CmdRoot().main(args)
        }

        fun initLogbackConfigFilePath() {
            val logbackConfFile = System.getProperty("logback.configurationFile", "")
            if ( logbackConfFile != "") {
                val log = LoggerFactory.getLogger("app")
                log.info("logback.configurationFile: ${logbackConfFile}")
            }
        }

    }
}