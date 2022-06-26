@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "film_category")
public open class FilmCategory(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var film_id: Short = 0

  @Column(
    nullable = false,
    length = 3,
    unique = true,
  )
  public var category_id: Short = 0

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
