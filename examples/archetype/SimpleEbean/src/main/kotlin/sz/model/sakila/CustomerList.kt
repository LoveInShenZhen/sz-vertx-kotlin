@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.View
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@View(name = "customer_list")
@DbComment("VIEW")
public open class CustomerList(
  dataSource: String = "",
) : Model(dataSource) {
  @DbComment("默认值: 0")
  @Column(
    nullable = false,
    length = 5,
  )
  public var id: Short? = null

  @Column(
    nullable = true,
    length = 91,
  )
  public var name: String? = null

  @Column(
    nullable = false,
    length = 50,
  )
  public var address: String = ""

  @Column(
    nullable = true,
    length = 10,
  )
  public var `zip code`: String? = null

  @Column(
    nullable = false,
    length = 20,
  )
  public var phone: String = ""

  @Column(
    nullable = false,
    length = 50,
  )
  public var city: String = ""

  @Column(
    nullable = false,
    length = 50,
  )
  public var country: String = ""

  @Column(
    nullable = false,
    length = 6,
  )
  public var notes: String = ""

  @Column(
    nullable = false,
    length = 3,
  )
  public var sid: Short = 0
}
