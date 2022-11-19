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
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "actor")
public open class Actor(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var actor_id: Short = 0

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

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
