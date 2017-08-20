package models

import io.ebean.ExpressionList
import io.ebean.Finder
import io.ebean.Model
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jodd.datetime.JDateTime
import sz.annotations.DBIndexed
import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version

//
// Created by kk on 17/8/20.
//

@Entity
class User : Model() {

    @Id
    var id: Long = 0

    @Version
    var version: Long? = null

    @WhenCreated
    var whenCreated: Timestamp? = null

    @WhenModified
    var whenModified: Timestamp? = null

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(32) DEFAULT '' COMMENT '真实姓名'")
    var real_name: String = ""

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(16) COMMENT '手机号码'", unique = true)
    var mobile: String? = null

    @Column(columnDefinition = "VARCHAR(128) DEFAULT NULL COMMENT '二次防字典工具加密后的密码'")
    var encrypt_pwd: String = ""

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(32) COMMENT '存管账户平台客户号'", unique = true)
    var pla_cust_id: String? = null

    @Column(columnDefinition = "VARCHAR(8) DEFAULT '' COMMENT '开户银行代号'")
    var open_bank_id: String = ""       // enum class: Banks

    @Column(columnDefinition = "VARCHAR(40) DEFAULT '' COMMENT '开户银行账号'")
    var open_acct_id: String = ""

    @Column(columnDefinition = "VARCHAR(2) DEFAULT '' COMMENT '证件类型'")
    var id_type: String = ""

    @Column(columnDefinition = "VARCHAR(64) COMMENT '证件号码'", unique = true)
    var id_no: String? = null

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(64) COMMENT '用户绑定的微信open_id'", unique = true)
    var wechat_open_id: String? = null

    @Column(columnDefinition = "DATETIME COMMENT '用户绑定的微信open_id时间'")
    var weixin_bind_time: JDateTime? = null

    @Column(columnDefinition = "DATETIME COMMENT '用户注册时间'")
    var regist_time: JDateTime? = null

    @Column(columnDefinition = "INTEGER DEFAULT 0 COMMENT '用户状态: 0-正常 1-禁用'", nullable = false)
    var user_status: Int = 0             // enum class: UserStatus

    companion object : Finder<Long, User>(User::class.java) {

        fun mobileExists(mobile: String): Boolean {
            return query().where().eq("mobile", mobile).findCount() == 1
        }

        fun byMobile(mobile: String): User? {
            return query().where().eq("mobile", mobile).findUnique()
        }

        fun byPlaCustId(plaCustId: String): User? {
            return query().where().eq("pla_cust_id", plaCustId).findUnique()
        }
    }
}
