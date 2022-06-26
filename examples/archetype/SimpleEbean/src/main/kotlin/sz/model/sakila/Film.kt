@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "film")
public open class Film(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var film_id: Short = 0

  @Column(
    nullable = false,
    length = 128,
  )
  public var title: String = ""

  @Column(
    nullable = true,
    length = 65535,
  )
  public var description: String? = null

  @Column(
    nullable = true,
    length = 4,
  )
  public var release_year: LocalDate? = null

  @Column(
    nullable = false,
    length = 3,
  )
  public var language_id: Short = 0

  @Column(
    nullable = true,
    length = 3,
  )
  public var original_language_id: Short? = null

  @DbComment("默认值: 3")
  @Column(
    nullable = false,
    length = 3,
  )
  public var rental_duration: Short? = null

  @DbComment("默认值: 4.99")
  @Column(
    nullable = false,
    length = 4,
  )
  public var rental_rate: BigDecimal? = null

  @Column(
    nullable = true,
    length = 5,
  )
  public var length: Short? = null

  @DbComment("默认值: 19.99")
  @Column(
    nullable = false,
    length = 5,
  )
  public var replacement_cost: BigDecimal? = null

  @DbComment("默认值: G")
  @Column(
    nullable = true,
    length = 5,
  )
  public var rating: String? = null

  @Column(
    nullable = true,
    length = 54,
  )
  public var special_features: String? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
