@file:Suppress("LocalVariableName")

package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import myquant.biz.BizFilterPubUsers
import myquant.biz.BizSwitchSiteAndGroup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

//
// Created by drago on 2023/4/18 018.
//
class CmdPubUsers : CliktCommand(help = "过滤出纯公版用户,执行用户组/站点切换", name = "pub-users") {
    private val log: Logger = LoggerFactory.getLogger("app")
    private lateinit var config: Config

    private val flag_switch by option("--switch", help = "执行用户组/站点切换").flag(default = false)
    private val csv_path by option("-o", help = "公版用户列表文件保存路径").default("")
    private val x_user_csv_path by option("-N", help = "被排除的非公版用户列表文件路径").default("")

    override fun run() {
        initConfig()

        val biz = BizFilterPubUsers()
        val pubUsers = biz.filterPubUsers()
        if (flag_switch) {
            log.info("切换公版用户到新站点")
            var last_check_progress_time = Instant.now()
            val total_count = pubUsers.size
            var finished_count = 0

            pubUsers.forEach { user ->
                val switchBiz = BizSwitchSiteAndGroup(config, user.id)
                switchBiz.run()

                finished_count++

                // 每3秒输出一下进度
                val now = Instant.now()
                if (now.toEpochMilli() - last_check_progress_time.toEpochMilli() > 3000) {
                    last_check_progress_time = now
                    val progress = String.format("%.2f", finished_count.toDouble() / total_count * 100)
                    BizFilterPubUsers.log.info("已对 $finished_count 个用户切换站点, 进度 $progress %")
                }
            }
        }
        if (csv_path != "") {
            log.info("保存公版用户列表到 $csv_path")
            biz.writeToCsvFile(csv_path, pubUsers)
        }
        if (x_user_csv_path != "") {
            log.info("保存排除用户的信息到 $x_user_csv_path")
            biz.writeExcludeUserInfoToCsv(x_user_csv_path)
        }
    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }
}