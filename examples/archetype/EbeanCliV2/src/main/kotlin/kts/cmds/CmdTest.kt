package kts.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ebean.DB
import models.query.QOrderReq
import kts.tools.toJsonPretty
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

        val tran = db.beginTransaction()
        tran.use {
            val tag = "C"
            val req = QOrderReq().usingTransaction(tran).where().raw("""JSON_CONTAINS(`posi_tag`,'"${tag}"', '${'$'}')""").findOne()
            log.info(req?.toJsonPretty())
        }

    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }

}