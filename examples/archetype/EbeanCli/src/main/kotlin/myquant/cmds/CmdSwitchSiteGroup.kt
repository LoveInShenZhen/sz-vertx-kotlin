@file:Suppress("LocalVariableName", "PrivatePropertyName")

package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import myquant.biz.BizSwitchSiteAndGroup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

//
// Created by drago on 2023/3/27 027.
//
class CmdSwitchSiteGroup : CliktCommand(help = "切换指定用户到新站点,新权限组", name = "switch-site-and-groups") {
    private val log: Logger = LoggerFactory.getLogger("app")
    private lateinit var config: Config

    private val user_ids by option(help = "用户id,多个用户id之间用逗号分割").default("")
    private val user_ids_file by option(help = "用户id文件,每行一个用户id")

    override fun run() {
        initConfig()

        user_ids.split(",").forEach { userId ->
            if (userId != "") {
                val biz = BizSwitchSiteAndGroup(config, userId.toLong())
                biz.run()
            }
        }

        if (user_ids_file != null) {
            File(user_ids_file!!).readLines().forEach { line ->
                val user_id_txt = line.trim()
                if (user_id_txt != "") {
                    val biz = BizSwitchSiteAndGroup(config, user_id_txt.toLong())
                    biz.run()
                }
            }
        }

        log.info("执行完毕")
    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }
}