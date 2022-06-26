@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Int
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "payment")
public open class Payment(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var payment_id: Short = 0

  @Column(
    nullable = false,
    length = 5,
  )
  public var customer_id: Short = 0

  @Column(
    nullable = false,
    length = 3,
  )
  public var staff_id: Short = 0

  @Column(
    nullable = true,
    length = 10,
  )
  public var rental_id: Int? = null

  @Column(
    nullable = false,
    length = 5,
  )
  public var amount: BigDecimal = BigDecimal.ZERO

  @Column(
    nullable = false,
    length = 19,
  )
  public var payment_date: LocalDateTime? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
