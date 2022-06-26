package sz.simple.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
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

    val config_file by option(names = arrayOf("--config", "-c"), help = "config file path").default("")

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
        if (config_file != "") {
            System.setProperty("config.file", config_file)
            log.info("config.file: ${config_file}")
            ConfigFactory.invalidateCaches()    // Use ConfigFactory.invalidateCaches() to force-reload system properties.
        }
        log.info("ebean props.file: ${System.getProperty("props.file", "undefined")}")
    }

}