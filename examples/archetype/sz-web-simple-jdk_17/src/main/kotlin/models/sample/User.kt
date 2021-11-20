package models.sample


import io.ebean.Finder
import io.ebean.Model
import io.ebean.Query
import io.ebean.annotation.DbComment
import io.ebean.annotation.Index
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
import javax.persistence.*

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

    @DbComment("用户UUID")
    @Column(length = 36, nullable = false, unique = true)
    var user_id: UUID = zeroUUID

    @Index
    @DbComment("用户名")
    @Column(length = 32, unique = true, nullable = false)
    var name: String = ""

    @DbComment("用户密码,sha1(user_id+密码明文)")
    @Column(length = 128, nullable = false)
    var encrypted_pwd: String = ""

    @DbComment("备注信息")
    @Lob
    @Column()
    var remarks: String = ""

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