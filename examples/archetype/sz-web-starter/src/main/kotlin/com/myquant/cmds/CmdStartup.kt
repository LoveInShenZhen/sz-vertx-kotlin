package com.myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.LoggerFactory
import sz.scaffold.Application

//
// Created by drago on 2024/9/5 周四.
//
class CmdStartup : CliktCommand(name = "startup", help = "启动服务") {
  val log = LoggerFactory.getLogger("app")!!

  override fun run() {
    Application.setupVertx()

      Application.regOnStartHandler(50) {
        log.info("服务启动")
      }

      Application.regOnStopHanlder {

        log.info("服务退出")
      }

    Application.runHttpServer()
    Application.setupOnStartAndOnStop()
  }

}
