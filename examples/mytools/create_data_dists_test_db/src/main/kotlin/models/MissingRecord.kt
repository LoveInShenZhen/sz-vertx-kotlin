@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import kotlin.Boolean
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "missing_record")
public open class MissingRecord() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 19,
    unique = true,
  )
  public var id: Long = 0

  @Column(
    name = "symbol",
    nullable = false,
    length = 32,
  )
  public var symbol: String = ""

  @Column(
    name = "trade_date",
    nullable = false,
    length = 10,
  )
  public var trade_date: LocalDate? = null

  @Column(
    name = "data_type",
    nullable = false,
    length = 32,
  )
  public var data_type: String = ""

  @DbComment("是否已经修正")
  @Column(
    name = "fixed",
    nullable = false,
    length = 1,
  )
  public var fixed: Boolean = false

  @DbComment("进行修正时,增加的备注说明")
  @Column(
    name = "remarks",
    nullable = false,
    length = 100,
  )
  public var remarks: String = ""
}
