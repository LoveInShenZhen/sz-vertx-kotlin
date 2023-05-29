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
import kotlin.String

@Entity
@Table(name = "gm_group")
public open class GmGroup() {
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

  @DbComment("用户组名称")
  @Column(
    name = "group_name",
    nullable = true,
    length = 64,
    unique = true,
  )
  public var group_name: String? = null

  @DbComment("用户组标签,多个标签之间用分号间隔")
  @Column(
    name = "tags",
    nullable = true,
    length = 65535,
  )
  public var tags: String? = null

  @DbComment("备注")
  @Column(
    name = "notes",
    nullable = true,
    length = 65535,
  )
  public var notes: String? = null
}
