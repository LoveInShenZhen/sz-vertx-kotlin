package sz.simple.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ebean.DB
import org.slf4j.LoggerFactory
import sz.model.broker_v1.query.QOrder
import sz.model.sakila.query.QActor
import java.io.File
import java.time.ZoneId

//
// Created by kk on 2022/5/28.
//
class CmdRoot : CliktCommand() {

    val log = LoggerFactory.getLogger("app")

    val config: Config
        get() {
            return ConfigFactory.load()
        }

    override fun run() {
        initConfig()
        val results1 = QActor().first_name.eq("BOB").findList()
        results1.forEach {
            println(it)
        }
        val results = QOrder(DB.byName("broker"))
            .account_id.notEqualTo("55923d94-005a-11ea-99d5-00163e0a4100")
            .orderBy().created_at.desc()
            .setMaxRows(3).findList()
        results.forEach {
            println(it)
            println()
        }
        println(results.last().updated_at?.atZone(ZoneId.of("Asia/Shanghai")))
    }

    fun initConfig() {
        log.info("working dir: ${File(".").absolutePath.trim('.')}")
        log.info("config.file: ${System.getProperty("config.file", "undefined")}")
        log.info("ebean props.file: ${System.getProperty("props.file", "undefined")}")
    }

}