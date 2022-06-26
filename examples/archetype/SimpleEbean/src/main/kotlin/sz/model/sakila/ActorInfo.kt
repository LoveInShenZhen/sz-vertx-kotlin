@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "actor_info")
@DbComment("VIEW")
public open class ActorInfo(
  dataSource: String = "",
) : Model(dataSource) {
  @DbComment("默认值: 0")
  @Column(
    nullable = false,
    length = 5,
  )
  public var actor_id: Short? = null

  @Column(
    nullable = false,
    length = 45,
  )
  public var first_name: String = ""

  @Column(
    nullable = false,
    length = 45,
  )
  public var last_name: String = ""

  @Column(
    nullable = true,
    length = 65535,
  )
  public var film_info: String? = null
}
