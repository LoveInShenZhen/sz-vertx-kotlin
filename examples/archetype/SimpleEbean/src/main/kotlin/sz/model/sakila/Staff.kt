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
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "staff")
public open class Staff(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 3,
    unique = true,
  )
  public var staff_id: Short = 0

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
    nullable = false,
    length = 5,
  )
  public var address_id: Short = 0

  @Column(
    nullable = true,
    length = 65535,
  )
  public var picture: ByteArray? = null

  @Column(
    nullable = true,
    length = 50,
  )
  public var email: String? = null

  @Column(
    nullable = false,
    length = 3,
  )
  public var store_id: Short = 0

  @DbComment("默认值: 1")
  @Column(
    nullable = false,
    length = 1,
  )
  public var active: Boolean? = null

  @Column(
    nullable = false,
    length = 16,
  )
  public var username: String = ""

  @Column(
    nullable = true,
    length = 40,
  )
  public var password: String? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
