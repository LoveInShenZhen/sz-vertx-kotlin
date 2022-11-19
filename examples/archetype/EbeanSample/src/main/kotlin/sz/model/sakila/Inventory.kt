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
@Table(name = "inventory")
public open class Inventory(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 8,
    unique = true,
  )
  public var inventory_id: Int = 0

  @Column(
    nullable = false,
    length = 5,
  )
  public var film_id: Short = 0

  @Column(
    nullable = false,
    length = 3,
  )
  public var store_id: Short = 0

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
