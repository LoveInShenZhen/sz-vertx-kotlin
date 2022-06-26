@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "film_list")
@DbComment("VIEW")
public open class FilmList(
  dataSource: String = "",
) : Model(dataSource) {
  @DbComment("默认值: 0")
  @Column(
    nullable = true,
    length = 5,
  )
  public var fid: Short? = null

  @Column(
    nullable = true,
    length = 128,
  )
  public var title: String? = null

  @Column(
    nullable = true,
    length = 65535,
  )
  public var description: String? = null

  @Column(
    nullable = false,
    length = 25,
  )
  public var category: String = ""

  @DbComment("默认值: 4.99")
  @Column(
    nullable = true,
    length = 4,
  )
  public var price: BigDecimal? = null

  @Column(
    nullable = true,
    length = 5,
  )
  public var length: Short? = null

  @DbComment("默认值: G")
  @Column(
    nullable = true,
    length = 5,
  )
  public var rating: String? = null

  @Column(
    nullable = true,
    length = 65535,
  )
  public var actors: String? = null
}
