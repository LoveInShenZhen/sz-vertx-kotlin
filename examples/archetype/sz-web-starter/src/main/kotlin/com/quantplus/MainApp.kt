package com.quantplus

import org.slf4j.LoggerFactory
import sz.scaffold.Application


//
// Created by drago on 2024/2/5.
//
class MainApp {

  companion object {

    val log = LoggerFactory.getLogger("app")!!

    @JvmStatic
    fun main(args: Array<String>) {
      setupSystemEncodingProperties()

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

    fun setupSystemEncodingProperties() {
      System.setProperty("native.encoding", "UTF-8")
      System.setProperty("file.encoding", "UTF-8")
      System.setProperty("sun.stdout.encoding", "UTF-8")
      System.setProperty("sun.stderr.encoding", "UTF-8")
      System.setProperty("sun.jnu.encoding", "UTF-8")
      System.setProperty("stderr.encoding", "UTF-8")
      System.setProperty("stdout.encoding", "UTF-8")

      System.getProperties().toSortedMap(compareBy {
        it.toString()
      }).forEach { (k, v) ->
        log.debug("-D{}={}", k, v)
      }
    }
  }
}
