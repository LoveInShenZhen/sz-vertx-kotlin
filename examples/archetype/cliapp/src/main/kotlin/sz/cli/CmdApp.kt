package sz.cli

import org.slf4j.LoggerFactory
import sz.cli.cmds.Hello
import java.io.File

//
// Created by kk on 2021/9/20.
//

val logger = LoggerFactory.getLogger("app")!!

class CmdApp {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val config_file = System.getProperty("config.file", "")
            if (config_file == "") {
                logger.info("config.file 未设置")
            } else {
                logger.info("config.file : $config_file")
            }

            logger.info("workingDir : ${File("").absolutePath}")

            Hello().main(args)
        }
    }

}