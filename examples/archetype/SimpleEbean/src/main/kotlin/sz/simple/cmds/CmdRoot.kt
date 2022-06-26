package sz.simple.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import sz.model.sakila.query.QActor
import java.io.File

//
// Created by kk on 2022/5/28.
//
class CmdRoot : CliktCommand() {

    val log = LoggerFactory.getLogger("app")

    val config:Config
    get() {
        return ConfigFactory.load()
    }

    override fun run() {
        initConfig()
        val cityList = QActor().setMaxRows(10).findList()
        cityList.forEach {
            println(cityList.toString())
        }
    }

    fun initConfig() {
        log.info("working dir: ${File(".").absolutePath.trim('.')}")
        log.info("config.file: ${System.getProperty("config.file", "undefined")}")
        log.info("ebean props.file: ${System.getProperty("props.file", "undefined")}")
    }

}