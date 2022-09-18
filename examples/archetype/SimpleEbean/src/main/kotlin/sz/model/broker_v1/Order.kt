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
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`order`")
public open class Order(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`strategy_id`",
    nullable = false,
    length = 36,
  )
  public var strategy_id: String = ""

  @Column(
    name = "`account_id`",
    nullable = false,
    length = 36,
  )
  public var account_id: String = ""

  @Column(
    name = "`account_name`",
    nullable = false,
    length = 36,
  )
  public var account_name: String = ""

  @Column(
    name = "`cl_ord_id`",
    nullable = false,
    length = 36,
  )
  public var cl_ord_id: String = ""

  @Column(
    name = "`order_id`",
    nullable = false,
    length = 36,
  )
  public var order_id: String = ""

  @Column(
    name = "`ex_ord_id`",
    nullable = false,
    length = 36,
  )
  public var ex_ord_id: String = ""

  @Column(
    name = "`symbol`",
    nullable = true,
    length = 36,
  )
  public var symbol: String? = null

  @Column(
    name = "`side`",
    nullable = true,
    length = 3,
  )
  public var side: Short? = null

  @Column(
    name = "`position_effect`",
    nullable = true,
    length = 3,
  )
  public var position_effect: Short? = null

  @Column(
    name = "`position_side`",
    nullable = true,
    length = 3,
  )
  public var position_side: Short? = null

  @Column(
    name = "`order_type`",
    nullable = true,
    length = 3,
  )
  public var order_type: Short? = null

  @Column(
    name = "`order_duration`",
    nullable = true,
    length = 3,
  )
  public var order_duration: Short? = null

  @Column(
    name = "`order_qualifier`",
    nullable = true,
    length = 3,
  )
  public var order_qualifier: Short? = null

  @Column(
    name = "`order_src`",
    nullable = true,
    length = 3,
  )
  public var order_src: Short? = null

  @Column(
    name = "`status`",
    nullable = true,
    length = 3,
  )
  public var status: Short? = null

  @Column(
    name = "`ord_rej_reason`",
    nullable = true,
    length = 3,
  )
  public var ord_rej_reason: Short? = null

  @Column(
    name = "`ord_rej_reason_detail`",
    nullable = true,
    length = 255,
  )
  public var ord_rej_reason_detail: String? = null

  @Column(
    name = "`price`",
    nullable = true,
    length = 22,
  )
  public var price: Double? = null

  @Column(
    name = "`stop_price`",
    nullable = true,
    length = 22,
  )
  public var stop_price: Double? = null

  @Column(
    name = "`order_style`",
    nullable = true,
    length = 10,
  )
  public var order_style: Int? = null

  @Column(
    name = "`volume`",
    nullable = true,
    length = 19,
  )
  public var volume: Long? = null

  @Column(
    name = "`value`",
    nullable = true,
    length = 22,
  )
  public var `value`: Double? = null

  @Column(
    name = "`percent`",
    nullable = true,
    length = 22,
  )
  public var percent: Double? = null

  @Column(
    name = "`target_volume`",
    nullable = true,
    length = 19,
  )
  public var target_volume: Long? = null

  @Column(
    name = "`target_value`",
    nullable = true,
    length = 22,
  )
  public var target_value: Double? = null

  @Column(
    name = "`target_percent`",
    nullable = true,
    length = 22,
  )
  public var target_percent: Double? = null

  @Column(
    name = "`filled_volume`",
    nullable = true,
    length = 19,
  )
  public var filled_volume: Long? = null

  @Column(
    name = "`filled_vwap`",
    nullable = true,
    length = 22,
  )
  public var filled_vwap: Double? = null

  @Column(
    name = "`filled_amount`",
    nullable = true,
    length = 22,
  )
  public var filled_amount: Double? = null

  @Column(
    name = "`filled_commission`",
    nullable = true,
    length = 22,
  )
  public var filled_commission: Double? = null

  @WhenCreated
  public var created_at: Instant? = null

  @WhenModified
  public var updated_at: Instant? = null

  @Column(
    name = "`algo_order_id`",
    nullable = false,
    length = 36,
  )
  public var algo_order_id: String = ""

  @DbComment("默认值: 0")
  @Column(
    name = "`order_business`",
    nullable = true,
    length = 3,
  )
  public var order_business: Short? = null

  @Column(
    name = "`position_src`",
    nullable = true,
    length = 3,
  )
  public var position_src: Short? = null
}
