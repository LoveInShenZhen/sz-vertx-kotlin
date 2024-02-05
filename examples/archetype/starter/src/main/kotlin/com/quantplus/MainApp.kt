package com.quantplus

//
// Created by drago on 2024/2/5.
//
class MainApp {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      initLogbackConfigFilePath()
      val encoding = System.getProperty("file.encoding")
      println(encoding)
      println("hello quant plus 中文测试+++")
    }

    fun initLogbackConfigFilePath() {
//      val logbackConfFile = System.getProperty("logback.configurationFile", "")
//      if (logbackConfFile != "") {
//        val log = LoggerFactory.getLogger("app")
//        log.info("logback.configurationFile: ${logbackConfFile}")
//      }
    }

  }
}
