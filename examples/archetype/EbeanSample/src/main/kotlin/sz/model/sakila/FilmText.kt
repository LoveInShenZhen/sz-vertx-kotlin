@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "film_text")
public open class FilmText(
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
    length = 255,
  )
  public var title: String = ""

  @Column(
    nullable = true,
    length = 65535,
  )
  public var description: String? = null
}
