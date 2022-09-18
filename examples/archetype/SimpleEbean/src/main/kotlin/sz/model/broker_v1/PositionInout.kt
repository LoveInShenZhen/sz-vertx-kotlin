@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package sz.model.broker_v1

import io.ebean.Model
import io.ebean.`annotation`.WhenCreated
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`position_inout`")
public open class PositionInout(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`account_id`",
    nullable = true,
    length = 255,
  )
  public var account_id: String? = null

  @Column(
    name = "`symbol`",
    nullable = true,
    length = 255,
  )
  public var symbol: String? = null

  @Column(
    name = "`side`",
    nullable = true,
    length = 10,
  )
  public var side: Int? = null

  @Column(
    name = "`volume`",
    nullable = true,
    length = 19,
  )
  public var volume: Long? = null

  @Column(
    name = "`volume_today`",
    nullable = true,
    length = 19,
  )
  public var volume_today: Long? = null

  @Column(
    name = "`available`",
    nullable = true,
    length = 19,
  )
  public var available: Long? = null

  @Column(
    name = "`available_today`",
    nullable = true,
    length = 19,
  )
  public var available_today: Long? = null

  @Column(
    name = "`vwap`",
    nullable = true,
    length = 22,
  )
  public var vwap: Double? = null

  @Column(
    name = "`comment`",
    nullable = true,
    length = 255,
  )
  public var comment: String? = null

  @WhenCreated
  public var created_at: Instant? = null
}
