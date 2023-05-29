@file:Suppress("LocalVariableName", "RemoveCurlyBracesFromTemplate")

package myquant.biz

import io.ebean.DB
import io.ebean.Database
import models.myquant.gmadmin.GmGroup
import models.myquant.gmadmin.GmUser
import models.myquant.gmadmin.query.QGmGroup
import models.myquant.gmadmin.query.QGmRUserGroup
import models.myquant.gmadmin.query.QGmUser
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

class ExcludeUser(val gmuser: GmUser, val excludeReason: String)

//
// Created by drago on 2023/4/18 018.
//
class BizFilterPubUsers() {

    // 券商机构名称列表
    private val org_list = listOf(
        "chinafortune",
        "yingda",
        "vanho",
        "shengang",
        "jiuzhou",
        "cindasc",
        "dongxing",
        "northeast",
        "tianfeng",
        "everbright"
    )

    private val org_user_guids = mutableMapOf<String, String>()    // key: user guid, value: orgCode 列表
    private val org_groups = mutableMapOf<Long, GmGroup>()  // 机构互访组
    private val myquant_db = DB.byName("myquant")    // 默认是掘金生产库
    private val n_pub_users = mutableListOf<ExcludeUser>()   // 被排除的非公版用户

    init {
        loadOrgUserGuids()
        laodOrgGroups()
    }

    // 加载所有券商机构库的用户 guid
    private fun loadOrgUserGuids() {
        org_list.forEach { orgName ->
            log.info("加载机构 $orgName 的用户 guid")
            val org_db = orgDB(orgName)
            log.debug(org_db.dataSource().toString())
            val org_users = QGmUser(org_db).select("guid").findList()
            log.debug("加载机构库 $orgName 用户, 共计 ${org_users.size} 条记录")
            org_users.forEach { user ->
                if (org_user_guids.containsKey(user.guid!!)) {
                    val orgNames = org_user_guids[user.guid]!!
                    org_user_guids[user.guid!!] = "$orgNames $orgName"
                } else {
                    org_user_guids[user.guid!!] = orgName
                }
            }
        }
    }

    // 加载券商机构互访组列表
    private fun laodOrgGroups() {
        log.info("加载券商机构互访组列表")
        QGmGroup(myquant_db).where()
            .group_name.like("机构互访%").findList().forEach { group ->
                org_groups[group.id] = group
            }
        log.info("加载券商机构互访组列表                   [OK]")
    }

    // 判断是否是公版用户
    private fun isPubUser(user: GmUser): Boolean {
        if (org_user_guids.contains(user.guid!!)) {
            val org_names = org_user_guids[user.guid!!]!!.trim()
            n_pub_users.add(ExcludeUser(user, "托管机构库[$org_names]中包含此用户的guid"))
            return false
        }

        if (user.note_for_redtree != null && user.note_for_redtree!!.contains("机构互访")) {
            n_pub_users.add(ExcludeUser(user, "用户note_for_redtree中有[机构互访]信息"))
            return false
        }

        val has_x_group = QGmRUserGroup(myquant_db).where().user_id.eq(user.id).findList().any { r ->
            if (r.group_id == 4L) {
                n_pub_users.add(ExcludeUser(user, "用户关联了 [service-group] 组"))
                return@any true
            }

            if (r.group_id == 46L) {
                n_pub_users.add(ExcludeUser(user, "用户关联了 [机构-东方财富-eastmoney] 组"))
                return@any true
            }

            if (org_groups.containsKey(r.id)) {
                n_pub_users.add(ExcludeUser(user, "用户关联了机构互访组 [${org_groups[r.id]!!.group_name}]"))
                return@any true
            }

            return@any false
        }

        if (has_x_group) {
            return false
        }

        return true
    }

    private fun orgDB(orgName: String): Database {
        return DB.byName(orgName)
    }

    fun filterPubUsers(): List<GmUser> {
        log.info("过滤公版用户")
        n_pub_users.clear()

        val users = mutableListOf<GmUser>()
        val total_count = QGmUser(myquant_db).findCount()
        var finished_count = 0

        var last_check_progress_time = Instant.now()
        QGmUser(myquant_db).findIterate().forEach { user ->
            if (isPubUser(user)) {
//                log.debug("用户 ${user.username} 是公版用户")
                users.add(user)
            }

            finished_count++

            // 每3秒输出一下进度
            val now = Instant.now()
            if (now.toEpochMilli() - last_check_progress_time.toEpochMilli() > 3000) {
                last_check_progress_time = now
                val progress = String.format("%.2f", finished_count.toDouble() / total_count * 100)
                log.info("已过滤 ${finished_count} 个用户, 总数 ${total_count}, 进度 $progress %")
            }
        }

        return users
    }

    fun writeToCsvFile(csvFpath: String, pubUsers: List<GmUser>) {
        val csvFormat =
            CSVFormat.DEFAULT.builder().setHeader("user_id", "user_name", "mobile", "note_for_redtree").build()
        val w = FileWriter(csvFpath)
        val csvPrinter = CSVPrinter(w, csvFormat)

        pubUsers.forEach { user ->
            csvPrinter.printRecord(user.id, user.username, user.mobile, user.note_for_redtree)
        }

        csvPrinter.close(true)
    }

    fun writeExcludeUserInfoToCsv(csvFpath: String) {
        val csvFormat =
            CSVFormat.DEFAULT.builder()
                .setHeader("user_id", "user_name", "mobile", "note_for_redtree", "exclude_reason").build()
        val w = FileWriter(csvFpath)
        val csvPrinter = CSVPrinter(w, csvFormat)

        n_pub_users.forEach { xuser ->
            csvPrinter.printRecord(
                xuser.gmuser.id,
                xuser.gmuser.username,
                xuser.gmuser.mobile,
                xuser.gmuser.note_for_redtree,
                xuser.excludeReason
            )
        }

        csvPrinter.close(true)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger("BizFilterPubUsers")
    }
}
