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
@Table(name = "language")
public open class Language(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 3,
    unique = true,
  )
  public var language_id: Short = 0

  @Column(
    nullable = false,
    length = 20,
  )
  public var name: String = ""

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
