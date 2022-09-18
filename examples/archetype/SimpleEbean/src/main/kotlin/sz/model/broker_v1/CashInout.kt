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
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "`cash_inout`")
public open class CashInout(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    name = "`account_id`",
    nullable = true,
    length = 255,
  )
  public var account_id: String? = null

  @Column(
    name = "`currency`",
    nullable = true,
    length = 10,
  )
  public var currency: Int? = null

  @Column(
    name = "`amount`",
    nullable = true,
    length = 22,
  )
  public var amount: Double? = null

  @Column(
    name = "`comment`",
    nullable = true,
    length = 255,
  )
  public var comment: String? = null

  @WhenCreated
  public var created_at: Instant? = null
}
