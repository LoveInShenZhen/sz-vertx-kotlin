package sz.simple

import org.slf4j.LoggerFactory
import sz.simple.cmds.CmdStartServer
import java.io.File

//
// Created by kk on 2022/5/29.
//
class MainApp {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            initLogbackConfigFilePath()
            CmdStartServer().main(args)
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