@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.broker_v1

import io.ebean.Model
import io.ebean.`annotation`.WhenCreated
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Double
import kotlin.Long
import kotlin.String

@Embeddable
public data class DividendLogUPK(
  @Column(
    name = "`account_id`",
    nullable = false,
    length = 255,
  )
  public var account_id: String = "",
  @Column(
    name = "`symbol`",
    nullable = false,
    length = 255,
  )
  public var symbol: String = "",
)

@MappedSuperclass
@Entity
@Table(name = "dividend_log")
public open class DividendLog(
  dataSource: String = "",
) : Model(dataSource) {
  @EmbeddedId
  public lateinit var dividendLogUPK: DividendLogUPK

  @Column(
    name = "`cash_div_rate`",
    nullable = true,
    length = 22,
  )
  public var cash_div_rate: Double? = null

  @Column(
    name = "`cash_div`",
    nullable = true,
    length = 22,
  )
  public var cash_div: Double? = null

  @Column(
    name = "`share_div_rate`",
    nullable = true,
    length = 22,
  )
  public var share_div_rate: Double? = null

  @Column(
    name = "`share_div`",
    nullable = true,
    length = 22,
  )
  public var share_div: Double? = null

  @WhenCreated
  public var created_at: Instant? = null

  @Column(
    name = "`volume`",
    nullable = true,
    length = 19,
  )
  public var volume: Long? = null
}
