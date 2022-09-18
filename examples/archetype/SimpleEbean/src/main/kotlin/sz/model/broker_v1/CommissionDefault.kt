@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package sz.model.broker_v1

import io.ebean.Model
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Double
import kotlin.Int
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`commission_default`")
public open class CommissionDefault(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`exchange`",
    nullable = true,
    length = 255,
  )
  public var exchange: String? = null

  @Column(
    name = "`sec_type`",
    nullable = true,
    length = 10,
  )
  public var sec_type: Int? = null

  @Column(
    name = "`sec_id`",
    nullable = true,
    length = 255,
  )
  public var sec_id: String? = null

  @Column(
    name = "`sec_name`",
    nullable = true,
    length = 255,
  )
  public var sec_name: String? = null

  @Column(
    name = "`open_fee`",
    nullable = true,
    length = 22,
  )
  public var open_fee: Double? = null

  @Column(
    name = "`open_fee_ratio`",
    nullable = true,
    length = 22,
  )
  public var open_fee_ratio: Double? = null

  @Column(
    name = "`close_fee`",
    nullable = true,
    length = 22,
  )
  public var close_fee: Double? = null

  @Column(
    name = "`close_fee_ratio`",
    nullable = true,
    length = 22,
  )
  public var close_fee_ratio: Double? = null

  @Column(
    name = "`close_today_fee`",
    nullable = true,
    length = 22,
  )
  public var close_today_fee: Double? = null

  @Column(
    name = "`close_today_fee_ratio`",
    nullable = true,
    length = 22,
  )
  public var close_today_fee_ratio: Double? = null

  @Column(
    name = "`min_fee`",
    nullable = true,
    length = 22,
  )
  public var min_fee: Double? = null
}
