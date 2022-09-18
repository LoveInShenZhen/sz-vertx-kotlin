@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package sz.model.broker_v1

import io.ebean.Model
import io.ebean.`annotation`.WhenCreated
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`delivery_log`")
public open class DeliveryLog(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`strategy_id`",
    nullable = true,
    length = 255,
  )
  public var strategy_id: String? = null

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
    name = "`cl_ord_id`",
    nullable = true,
    length = 255,
  )
  public var cl_ord_id: String? = null

  @Column(
    name = "`order_id`",
    nullable = true,
    length = 255,
  )
  public var order_id: String? = null

  @Id
  @Column(
    name = "`exec_id`",
    nullable = false,
    length = 255,
    unique = true,
  )
  public var exec_id: String = ""

  @Column(
    name = "`symbol`",
    nullable = true,
    length = 255,
  )
  public var symbol: String? = null

  @Column(
    name = "`position_effect`",
    nullable = true,
    length = 10,
  )
  public var position_effect: Int? = null

  @Column(
    name = "`side`",
    nullable = true,
    length = 10,
  )
  public var side: Int? = null

  @Column(
    name = "`ord_rej_reason`",
    nullable = true,
    length = 10,
  )
  public var ord_rej_reason: Int? = null

  @Column(
    name = "`ord_rej_reason_detail`",
    nullable = true,
    length = 255,
  )
  public var ord_rej_reason_detail: String? = null

  @Column(
    name = "`exec_type`",
    nullable = true,
    length = 10,
  )
  public var exec_type: Int? = null

  @Column(
    name = "`price`",
    nullable = true,
    length = 22,
  )
  public var price: Double? = null

  @Column(
    name = "`volume`",
    nullable = true,
    length = 19,
  )
  public var volume: Long? = null

  @Column(
    name = "`amount`",
    nullable = true,
    length = 22,
  )
  public var amount: Double? = null

  @Column(
    name = "`commission`",
    nullable = true,
    length = 22,
  )
  public var commission: Double? = null

  @Column(
    name = "`cost`",
    nullable = true,
    length = 22,
  )
  public var cost: Double? = null

  @WhenCreated
  public var created_at: Instant? = null
}
