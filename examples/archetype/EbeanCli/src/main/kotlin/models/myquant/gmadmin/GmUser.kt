@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models.myquant.gmadmin

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import io.ebean.`annotation`.WhenModified
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.Long
import kotlin.Short
import kotlin.String

@Entity
@Table(name = "gm_user")
public open class GmUser() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 20,
    unique = true,
  )
  public var id: Long = 0

  @WhenCreated
  public var created_at: LocalDateTime? = null

  @WhenModified
  public var updated_at: LocalDateTime? = null

  @DbComment("用户名,全局唯一")
  @Column(
    name = "username",
    nullable = true,
    length = 100,
    unique = true,
  )
  public var username: String? = null

  @DbComment("密码")
  @Column(
    name = "password",
    nullable = true,
    length = 128,
  )
  public var password: String? = null

  @DbComment("昵称")
  @Column(
    name = "nick_name",
    nullable = true,
    length = 64,
  )
  public var nick_name: String? = null

  @DbComment("用户邮箱,未设定时为null,全局唯一")
  @Column(
    name = "email",
    nullable = true,
    length = 64,
  )
  public var email: String? = null

  @DbComment("用户手机号码,未设定时为null,全局唯一")
  @Column(
    name = "mobile",
    nullable = true,
    length = 20,
  )
  public var mobile: String? = null

  @DbComment("用户是否有效,默认有效")
  @Column(
    name = "is_active",
    nullable = true,
    length = 3,
  )
  public var is_active: Short? = null

  @DbComment("用户头像图片资源url")
  @Column(
    name = "avatar_file",
    nullable = true,
    length = 65535,
  )
  public var avatar_file: String? = null

  @DbComment("账户创建时间")
  @Column(
    name = "date_joined",
    nullable = true,
    length = 19,
  )
  public var date_joined: LocalDateTime? = null

  @DbComment("个人简介")
  @Column(
    name = "introduction",
    nullable = true,
    length = 65535,
  )
  public var introduction: String? = null

  @DbComment("备注")
  @Column(
    name = "note_for_redtree",
    nullable = true,
    length = 65535,
  )
  public var note_for_redtree: String? = null

  @DbComment("最后一次登录时间")
  @Column(
    name = "last_login",
    nullable = true,
    length = 19,
  )
  public var last_login: LocalDateTime? = null

  @DbComment("全局唯一uuid，后续改为唯一索引")
  @Column(
    name = "guid",
    nullable = true,
    length = 36,
    unique = true,
  )
  public var guid: String? = null
}
