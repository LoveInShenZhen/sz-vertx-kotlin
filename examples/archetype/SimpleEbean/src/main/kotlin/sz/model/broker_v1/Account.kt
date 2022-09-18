@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package sz.model.broker_v1

import io.ebean.Model
import io.ebean.`annotation`.WhenCreated
import io.ebean.`annotation`.WhenModified
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`account`")
public open class Account(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    name = "`account_id`",
    nullable = false,
    length = 255,
    unique = true,
  )
  public var account_id: String = ""

  @Column(
    name = "`account_name`",
    nullable = true,
    length = 255,
  )
  public var account_name: String? = null

  @Column(
    name = "`title`",
    nullable = true,
    length = 255,
  )
  public var title: String? = null

  @Column(
    name = "`intro`",
    nullable = true,
    length = 255,
  )
  public var intro: String? = null

  @Column(
    name = "`comment`",
    nullable = true,
    length = 255,
  )
  public var comment: String? = null

  @WhenCreated
  public var created_at: Instant? = null

  @WhenModified
  public var updated_at: Instant? = null

  @Column(
    name = "`engine_id`",
    nullable = true,
    length = 255,
  )
  public var engine_id: String? = null

  @Column(
    name = "`user_id`",
    nullable = true,
    length = 255,
  )
  public var user_id: String? = null

  @Column(
    name = "`exchanges`",
    nullable = true,
    length = 65535,
  )
  public var exchanges: String? = null

  @Column(
    name = "`sec_types`",
    nullable = true,
    length = 65535,
  )
  public var sec_types: String? = null

  @Column(
    name = "`info`",
    nullable = true,
    length = 65535,
  )
  public var info: String? = null
}
