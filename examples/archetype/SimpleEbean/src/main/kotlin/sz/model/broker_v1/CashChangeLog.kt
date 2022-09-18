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
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`cash_change_log`")
public open class CashChangeLog(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`account_id`",
    nullable = true,
    length = 36,
  )
  public var account_id: String? = null

  @Column(
    name = "`account_name`",
    nullable = true,
    length = 36,
  )
  public var account_name: String? = null

  @Column(
    name = "`currency`",
    nullable = true,
    length = 10,
  )
  public var currency: Int? = null

  @Column(
    name = "`nav`",
    nullable = true,
    length = 22,
  )
  public var nav: Double? = null

  @Column(
    name = "`pnl`",
    nullable = true,
    length = 22,
  )
  public var pnl: Double? = null

  @Column(
    name = "`fpnl`",
    nullable = true,
    length = 22,
  )
  public var fpnl: Double? = null

  @Column(
    name = "`frozen`",
    nullable = true,
    length = 22,
  )
  public var frozen: Double? = null

  @Column(
    name = "`order_frozen`",
    nullable = true,
    length = 22,
  )
  public var order_frozen: Double? = null

  @Column(
    name = "`available`",
    nullable = true,
    length = 22,
  )
  public var available: Double? = null

  @Column(
    name = "`cum_inout`",
    nullable = true,
    length = 22,
  )
  public var cum_inout: Double? = null

  @Column(
    name = "`cum_trade`",
    nullable = true,
    length = 22,
  )
  public var cum_trade: Double? = null

  @Column(
    name = "`cum_pnl`",
    nullable = true,
    length = 22,
  )
  public var cum_pnl: Double? = null

  @Column(
    name = "`cum_commission`",
    nullable = true,
    length = 22,
  )
  public var cum_commission: Double? = null

  @Column(
    name = "`last_trade`",
    nullable = true,
    length = 22,
  )
  public var last_trade: Double? = null

  @Column(
    name = "`last_pnl`",
    nullable = true,
    length = 22,
  )
  public var last_pnl: Double? = null

  @Column(
    name = "`last_commission`",
    nullable = true,
    length = 22,
  )
  public var last_commission: Double? = null

  @Column(
    name = "`last_inout`",
    nullable = true,
    length = 22,
  )
  public var last_inout: Double? = null

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

  @WhenCreated
  public var created_at: Instant? = null

  @WhenModified
  public var updated_at: Instant? = null

  @Column(
    name = "`balance`",
    nullable = true,
    length = 22,
  )
  public var balance: Double? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    name = "`ts_update`",
    nullable = false,
    length = 19,
  )
  public var ts_update: Instant? = null

  @Column(
    name = "`market_value`",
    nullable = true,
    length = 255,
  )
  public var market_value: Double? = null
}
