@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package sz.model.sakila

import io.ebean.Model
import io.ebean.`annotation`.DbComment
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.MappedSuperclass
import javax.persistence.Table
import kotlin.Short
import kotlin.String

@Embeddable
public data class FilmActorUPK(
  @Column(
    nullable = false,
    length = 5,
  )
  public var actor_id: Short = 0,
  @Column(
    nullable = false,
    length = 5,
  )
  public var film_id: Short = 0,
)

@MappedSuperclass
@Entity
@Table(name = "film_actor")
public open class FilmActor(
  dataSource: String = "",
) : Model(dataSource) {
  @EmbeddedId
  public lateinit var filmActorUPK: FilmActorUPK

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
