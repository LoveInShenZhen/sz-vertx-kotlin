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

@Entity
@Table(name = "gm_r_user_group")
public open class GmRUserGroup() {
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

  @DbComment("用户id")
  @Column(
    name = "user_id",
    nullable = true,
    length = 19,
  )
  public var user_id: Long? = null

  @DbComment("组id")
  @Column(
    name = "group_id",
    nullable = true,
    length = 19,
  )
  public var group_id: Long? = null

  @DbComment("绑定关系生效时间")
  @Column(
    name = "started_at",
    nullable = true,
    length = 19,
  )
  public var started_at: LocalDateTime? = null

  @DbComment("绑定关系过期时间")
  @Column(
    name = "expires_at",
    nullable = true,
    length = 19,
  )
  public var expires_at: LocalDateTime? = null
}
