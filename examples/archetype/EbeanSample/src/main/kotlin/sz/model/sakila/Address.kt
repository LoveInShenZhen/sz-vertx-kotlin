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
import kotlin.ByteArray
import kotlin.Short
import kotlin.String

@MappedSuperclass
@Entity
@Table(name = "address")
public open class Address(
  dataSource: String = "",
) : Model(dataSource) {
  @Id
  @Column(
    nullable = false,
    length = 5,
    unique = true,
  )
  public var address_id: Short = 0

  @Column(
    nullable = false,
    length = 50,
  )
  public var address: String = ""

  @Column(
    nullable = true,
    length = 50,
  )
  public var address2: String? = null

  @Column(
    nullable = false,
    length = 20,
  )
  public var district: String = ""

  @Column(
    nullable = false,
    length = 5,
  )
  public var city_id: Short = 0

  @Column(
    nullable = true,
    length = 10,
  )
  public var postal_code: String? = null

  @Column(
    nullable = false,
    length = 20,
  )
  public var phone: String = ""

  @Column(
    nullable = false,
    length = 65535,
  )
  public var location: ByteArray? = null

  @DbComment("默认值: CURRENT_TIMESTAMP")
  @Column(
    nullable = false,
    length = 19,
  )
  public var last_update: LocalDateTime? = null
}
