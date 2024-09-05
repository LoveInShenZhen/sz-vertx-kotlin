package com.myquant

import com.github.ajalt.clikt.core.subcommands
import com.myquant.cmds.CmdRoot
import com.myquant.cmds.CmdStartup
import myquant.cmds.CmdTest
import myquant.cmds.CmdVersion
import org.slf4j.LoggerFactory
import java.io.File


//
// Created by drago on 2024/2/5.
//
class MainApp {

  companion object {

    val log = LoggerFactory.getLogger("app")!!

    @JvmStatic
    fun main(args: Array<String>) {
      initLogbackConfigFilePath()
      CmdRoot()
        .subcommands(
          CmdStartup(),
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
