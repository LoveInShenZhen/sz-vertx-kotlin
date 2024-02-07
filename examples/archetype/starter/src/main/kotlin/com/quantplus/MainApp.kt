package com.quantplus

import org.slf4j.LoggerFactory


//
// Created by drago on 2024/2/5.
//
class MainApp {

  companion object {

    val log = LoggerFactory.getLogger("app")!!

    @JvmStatic
    fun main(args: Array<String>) {
//      setupSystemEncodingProperties()

      System.getProperties().toSortedMap(compareBy {
        it.toString()
      }).forEach { (k, v) ->
        log.debug("-D{}={}", k, v)
      }

      log.warn("模拟有警告信息")
      log.info("hello quant plus 中文测试+++")
      log.error("模拟有错误信息")
    }

    fun setupSystemEncodingProperties() {
      System.setProperty("native.encoding", "UTF-8")
      System.setProperty("file.encoding", "UTF-8")
      System.setProperty("sun.stdout.encoding", "UTF-8")
      System.setProperty("sun.stderr.encoding", "UTF-8")
      System.setProperty("sun.jnu.encoding", "UTF-8")
      System.setProperty("stderr.encoding", "UTF-8")
      System.setProperty("stdout.encoding", "UTF-8")
    }
  }
}
