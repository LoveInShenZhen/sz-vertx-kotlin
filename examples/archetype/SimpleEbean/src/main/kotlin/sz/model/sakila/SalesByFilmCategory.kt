@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.View
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import kotlin.String

@MappedSuperclass
@Entity
@View(name = "sales_by_film_category")
@DbComment("VIEW")
public open class SalesByFilmCategory(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    nullable = false,
    length = 25,
  )
  public var category: String = ""

  @Column(
    nullable = true,
    length = 27,
  )
  public var total_sales: BigDecimal? = null
}
