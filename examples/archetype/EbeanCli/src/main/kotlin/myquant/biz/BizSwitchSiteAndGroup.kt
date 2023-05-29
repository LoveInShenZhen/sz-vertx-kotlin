@file:Suppress("DuplicatedCode", "LocalVariableName")

package myquant.biz

import com.typesafe.config.Config
import io.ebean.DB
import models.myquant.gmadmin.GmRUserGroup
import models.myquant.gmadmin.query.QGmRUserGroup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

//
// Created by drago on 2023/3/27 027.
//
class BizSwitchSiteAndGroup(val cfg: Config, val userId: Long) {

    private val db = DB.getDefault()

    private val normalGroupId: Long
        get() = cfg.getLong("groups.normal_group.id")

    private val paidGroupId: Long
        get() = cfg.getLong("groups.paid_group.id")

    private val bigDataGroupId: Long
        get() = cfg.getLong("groups.bigdata_group.id")

    private val internalTestGroupId: Long
        get() = cfg.getLong("groups.internal_test_group.id")

    private val newSiteGroupId: Long
        get() = cfg.getLong("groups.new_site_group.id")

    private val trialVerGroupId: Long
        get() = cfg.getLong("groups.trial_ver_group.id")

    private val proVerGroupId: Long
        get() = cfg.getLong("groups.pro_ver_group.id")

    private val entVerGroupId: Long
        get() = cfg.getLong("groups.enterprise_ver_group.id")

    private val dsLive4000GroupId: Long
        get() = cfg.getLong("groups.ds_live_4000_group.id")

    private val user_groups = mutableMapOf<Long, GmRUserGroup>()    // key: group_id

    fun run() {
        db.beginTransaction().use { tran ->
            loadFromDb()

            bindForNormalGroupUser()
            bindForPaidGroupUser()
            bindForBigdataGroupUser()
            bindForInternalTestGroup()

            tran.commit()
        }
    }

    private fun loadFromDb() {
        // 从数据库加载当前用户的所以有用户组关联关系
        QGmRUserGroup(db).where().user_id.eq(userId).findList().forEach {
            user_groups[it.group_id!!] = it
        }
    }

    private fun bindForNormalGroupUser() {
        // 若用户存在以下用户组  【normal-group】 解绑后，绑定至 【新站点组】、【体验版权限组】，不用配置绑定期限
        if (user_groups.containsKey(normalGroupId)) {
            val now = LocalDateTime.now()

            if (user_groups.containsKey(newSiteGroupId).not()) {
                val r_site_group = GmRUserGroup()
                r_site_group.created_at = now
                r_site_group.updated_at = now
                r_site_group.user_id = userId
                r_site_group.group_id = newSiteGroupId

                db.save(r_site_group)
                log.debug("用户 $userId 绑定到【新站点组】")

                user_groups[newSiteGroupId] = r_site_group
            }

            if (user_groups.containsKey(trialVerGroupId).not()) {
                val r_trial_group = GmRUserGroup()
                r_trial_group.created_at = now
                r_trial_group.updated_at = now
                r_trial_group.user_id = userId
                r_trial_group.group_id = trialVerGroupId

                db.save(r_trial_group)
                log.debug("用户 $userId 绑定到【体验版权限组】")

                user_groups[trialVerGroupId] = r_trial_group
            }

            val r_normal_group = user_groups[normalGroupId]!!
            db.delete(r_normal_group)
            log.debug("用户 $userId 与【normal-group】解除绑定")
            user_groups.remove(normalGroupId)
        }
    }

    private fun bindForPaidGroupUser() {
        // 若用户存在 【paid-group】 组 解绑后，则绑定至 【新站点组】、【专业版权限组】，【专业版权限组】需要继承【paid-group】的绑定期限
        if (user_groups.containsKey(paidGroupId)) {
            val now = LocalDateTime.now()
            val r_paid_group = user_groups[paidGroupId]!!

            if (user_groups.containsKey(newSiteGroupId).not()) {
                val r_site_group = GmRUserGroup()
                r_site_group.created_at = now
                r_site_group.updated_at = now
                r_site_group.user_id = userId
                r_site_group.group_id = newSiteGroupId

                db.save(r_site_group)
                log.debug("用户 $userId 绑定到【新站点组】")

                user_groups[newSiteGroupId] = r_site_group
            }

            if (user_groups.containsKey(proVerGroupId).not()) {
                val r_pro_group = GmRUserGroup()
                r_pro_group.created_at = now
                r_pro_group.updated_at = now
                r_pro_group.user_id = userId
                r_pro_group.group_id = proVerGroupId
                r_pro_group.started_at = r_paid_group.started_at
                r_pro_group.expires_at = r_paid_group.expires_at

                db.save(r_pro_group)
                log.debug("用户 $userId 绑定到【专业版权限组】")

                user_groups[proVerGroupId] = r_pro_group
            }

            db.delete(r_paid_group)
            log.debug("用户 $userId 与【paid-group】解除绑定")
            user_groups.remove(paidGroupId)
        }
    }


    private fun bindForBigdataGroupUser() {
        // 若用户存在 【bigdata-group】 组 解绑后，则绑定至 【新站点组】、【机构版权限组】、【行情订阅权限组-4000】，【机构版权限组】、【行情订阅权限组-4000】需要继承【bigdata-group】的绑定期限
        if (user_groups.containsKey(bigDataGroupId)) {
            val now = LocalDateTime.now()
            val r_bigdata_group = user_groups[bigDataGroupId]!!

            if (user_groups.containsKey(newSiteGroupId).not()) {
                val r_site_group = GmRUserGroup()
                r_site_group.created_at = now
                r_site_group.updated_at = now
                r_site_group.user_id = userId
                r_site_group.group_id = newSiteGroupId

                db.save(r_site_group)
                log.debug("用户 $userId 绑定到【新站点组】")

                user_groups[newSiteGroupId] = r_site_group
            }

            if (user_groups.containsKey(entVerGroupId).not()) {
                val r_ent_group = GmRUserGroup()
                r_ent_group.created_at = now
                r_ent_group.updated_at = now
                r_ent_group.user_id = userId
                r_ent_group.group_id = entVerGroupId
                r_ent_group.started_at = r_bigdata_group.started_at
                r_ent_group.expires_at = r_bigdata_group.expires_at

                db.save(r_ent_group)
                log.debug("用户 $userId 绑定到【机构版权限组】")

                user_groups[entVerGroupId] = r_ent_group
            }

            if (user_groups.containsKey(dsLive4000GroupId).not()) {
                val r_live_4k_group = GmRUserGroup()
                r_live_4k_group.created_at = now
                r_live_4k_group.updated_at = now
                r_live_4k_group.user_id = userId
                r_live_4k_group.group_id = dsLive4000GroupId
                r_live_4k_group.started_at = r_bigdata_group.started_at
                r_live_4k_group.expires_at = r_bigdata_group.expires_at

                db.save(r_live_4k_group)
                log.debug("用户 $userId 绑定到【行情订阅权限组-4000】")

                user_groups[dsLive4000GroupId] = r_live_4k_group
            }

            db.delete(r_bigdata_group)
            log.debug("用户 $userId 与【bigdata-group】解除绑定")
            user_groups.remove(bigDataGroupId)
        }
    }

    private fun bindForInternalTestGroup() {
        // 若用户存在 用户组 【新版API内测组】，则解绑，绑定至 【新站点组】、【体验版权限组】，不用配置绑定期限
        if (user_groups.containsKey(internalTestGroupId)) {
            val now = LocalDateTime.now()

            if (user_groups.containsKey(newSiteGroupId).not()) {
                val r_site_group = GmRUserGroup()
                r_site_group.created_at = now
                r_site_group.updated_at = now
                r_site_group.user_id = userId
                r_site_group.group_id = newSiteGroupId

                db.save(r_site_group)
                log.debug("用户 $userId 绑定到【新站点组】")

                user_groups[newSiteGroupId] = r_site_group
            }

            if (user_groups.containsKey(trialVerGroupId).not()) {
                val r_trial_group = GmRUserGroup()
                r_trial_group.created_at = now
                r_trial_group.updated_at = now
                r_trial_group.user_id = userId
                r_trial_group.group_id = trialVerGroupId

                db.save(r_trial_group)
                log.debug("用户 $userId 绑定到【体验版权限组】")

                user_groups[trialVerGroupId] = r_trial_group
            }

            val r_internal_test_group = user_groups[internalTestGroupId]!!
            db.delete(r_internal_test_group)
            log.debug("用户 $userId 与【新版API内测组】解除绑定")
            user_groups.remove(internalTestGroupId)
        }
    }


    companion object {
        val log: Logger = LoggerFactory.getLogger("app.BizSwitchSiteAndGroup")
    }
}