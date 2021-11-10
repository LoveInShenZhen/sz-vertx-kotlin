package models.sample


import io.ebean.Finder
import io.ebean.Model
import io.ebean.Query
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import jodd.crypt.DigestEngine
import jodd.datetime.JDateTime
import models.sample.query.QUser

import sz.annotations.DBIndexed
import sz.ebean.DB
import sz.scaffold.ext.zeroUUID
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Version

//
// This is a sample model class.
//

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
@Entity
class User(dataSource: String = "") : Model(dataSource) {

    @Id
    var id: Long = 0

    @Version
    var version: Long? = null

    @WhenCreated
//    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    var whenCreated: Timestamp? = null

    @WhenModified
//    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", nullable = false)
    var whenModified: Timestamp? = null

    @DBIndexed
    @Column(columnDefinition = "CHAR(36) COMMENT '用户UUID'", nullable = false)
    var user_id: UUID = zeroUUID

    @Column(columnDefinition = "VARCHAR(32) COMMENT '用户名'", nullable = false)
    var name: String = ""

    @Column(columnDefinition = "VARCHAR(128) COMMENT '用户密码,sha1(user_id+密码明文)'")
    var encrypted_pwd: String? = null

    @Column(columnDefinition = "TEXT COMMENT '备注信息'")
    var remarks: String? = null

    fun updatePwd(newPwd: String): User {
        encrypted_pwd = DigestEngine.sha1().digestString("$user_id$newPwd")
        return this
    }

    fun verifyPwd(pwd: String): Boolean {
        val calcPwd = DigestEngine.sha1().digestString("$user_id$pwd")
        return encrypted_pwd == calcPwd
    }

    companion object {

        fun new(dsName: String = DB.currentDataSource()): User {
            return User(dsName)
        }

        fun finder(dsName: String = DB.currentDataSource()): Finder<Long, User> {
            return DB.finder(dsName)
        }

        fun query(dsName: String = DB.currentDataSource()): Query<User> {
            return finder(dsName).query()
        }

        fun queryBean(dsName: String = DB.currentDataSource()): QUser {
            return QUser(DB.byDataSource(dsName))
        }

        fun findByName(name: String): User? {
            return queryBean().name.eq(name).findOne()
        }

        fun valid(name: String, pwd: String): Boolean {
            val user = queryBean()
                .name.eq(name)
                .findOne()
            if (user == null || user.verifyPwd(pwd).not()) {
                return false
            }
            return true
        }
    }
}