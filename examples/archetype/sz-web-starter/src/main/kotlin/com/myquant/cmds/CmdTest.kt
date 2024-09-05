package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.LoggerFactory

//
// Created by drago on 2024/8/8 周四.
//
class CmdTest : CliktCommand(name = "test", help = "仅开发时临时测试") {
  val log = LoggerFactory.getLogger("app")!!

  override fun run() {
    log.info("todo: 开发时临时测试")
  }
}
