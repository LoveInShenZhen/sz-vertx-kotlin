@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package sz.model.broker_v1

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import io.ebean.`annotation`.WhenModified
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
@Table(name = "`position_change_log`")
public open class PositionChangeLog(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`account_id`",
    nullable = true,
    length = 255,
  )
  public var account_id: String? = null

  @Column(
    name = "`account_name`",
    nullable = true,
    length = 255,
  )
  public var account_name: String? = null

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
    name = "`vwap`",
    nullable = true,
    length = 22,
  )
  public var vwap: Double? = null

  @Column(
    name = "`amount`",
    nullable = true,
    length = 22,
  )
  public var amount: Double? = null

  @Column(
    name = "`price`",
    nullable = true,
    length = 22,
  )
  public var price: Double? = null

  @Column(
    name = "`fpnl`",
    nullable = true,
    length = 22,
  )
  public var fpnl: Double? = null

  @Column(
    name = "`fpnl_open`",
    nullable = true,
    length = 22,
  )
  public var fpnl_open: Double? = null

  @Column(
    name = "`cost`",
    nullable = true,
    length = 22,
  )
  public var cost: Double? = null

  @Column(
    name = "`order_frozen`",
    nullable = true,
    length = 19,
  )
  public var order_frozen: Long? = null

  @Column(
    name = "`order_frozen_today`",
    nullable = true,
    length = 19,
  )
  public var order_frozen_today: Long? = null

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
    name = "`available_now`",
    nullable = true,
    length = 19,
  )
  public var available_now: Long? = null

  @Column(
    name = "`last_price`",
    nullable = true,
    length = 22,
  )
  public var last_price: Double? = null

  @Column(
    name = "`last_volume`",
    nullable = true,
    length = 19,
  )
  public var last_volume: Long? = null

  @Column(
    name = "`last_inout`",
    nullable = true,
    length = 19,
  )
  public var last_inout: Long? = null

  @Column(
    name = "`change_reason`",
    nullable = true,
    length = 10,
  )
  public var change_reason: Int? = null

  @Column(
    name = "`change_event_id`",
    nullable = true,
    length = 255,
  )
  public var change_event_id: String? = null

  @Column(
    name = "`has_dividend`",
    nullable = true,
    length = 10,
  )
  public var has_dividend: Int? = null

  @WhenCreated
  public var created_at: Instant? = null

  @WhenModified
  public var updated_at: Instant? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    name = "`ts_update`",
    nullable = false,
    length = 19,
  )
  public var ts_update: Instant? = null

  @Column(
    name = "`vwap_diluted`",
    nullable = true,
    length = 22,
  )
  public var vwap_diluted: Double? = null

  @Column(
    name = "`vwap_open`",
    nullable = true,
    length = 22,
  )
  public var vwap_open: Double? = null

  @Column(
    name = "`market_value`",
    nullable = true,
    length = 255,
  )
  public var market_value: Double? = null
}
