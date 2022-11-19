@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
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
@Table(name = "rental")
public open class Rental(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 10,
    unique = true,
  )
  public var rental_id: Int = 0

  @Column(
    nullable = false,
    length = 19,
    unique = true,
  )
  public var rental_date: LocalDateTime? = null

  @Column(
    nullable = false,
    length = 8,
    unique = true,
  )
  public var inventory_id: Int = 0

  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var customer_id: Short = 0

  @Column(
    nullable = true,
    length = 19,
  )
  public var return_date: LocalDateTime? = null

  @Column(
    nullable = false,
    length = 3,
  )
  public var staff_id: Short = 0

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
