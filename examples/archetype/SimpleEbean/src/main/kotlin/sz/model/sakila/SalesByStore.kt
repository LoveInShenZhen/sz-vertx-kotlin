@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "sales_by_store")
@DbComment("VIEW")
public open class SalesByStore(
  dataSource: String = "",
) : Model(dataSource) {
  @Column(
    nullable = true,
    length = 101,
  )
  public var store: String? = null

  @Column(
    nullable = true,
    length = 91,
  )
  public var manager: String? = null

  @Column(
    nullable = true,
    length = 27,
  )
  public var total_sales: BigDecimal? = null
}
