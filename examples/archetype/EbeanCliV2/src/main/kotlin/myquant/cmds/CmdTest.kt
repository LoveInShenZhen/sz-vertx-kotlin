package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ebean.DB
import models.OrderReq
import models.query.QOrderReq
import myquant.tools.Json
import myquant.tools.toJsonPretty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

//
// Created by drago on 2022/12/22 022.
//
class CmdTest : CliktCommand(help = "临时测试代码", name = "test") {
    val log: Logger = LoggerFactory.getLogger("app")


    lateinit var config: Config

    override fun run() {
        initConfig()

        log.info("===================================")

        val db = DB.byName("ebean_gen_test")

//        db.beginTransaction().use { tran ->
//            val req = OrderReq()
//            req.request_id = UUID.randomUUID().toString()
//            req.order_remark = "备注"
//            req.strategy_name = "捡垃圾策略"
//            req.posi_tag = Json.createArrayNode()
//
//            req.posi_tag!!.add("A")
//            req.posi_tag!!.add("B")
//            req.posi_tag!!.add("C")
//            req.posi_tag!!.add("D")
//
//            db.save(req)
//
//            tran.commit()
//            log.info("新增一条记录")
//        }

        db.beginTransaction().use {
            val req = QOrderReq(db).where().raw("""JSON_CONTAINS(`posi_tag`,'"C"', '${'$'}')""").findOne()
            log.info(req?.toJsonPretty())
        }

    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }

}