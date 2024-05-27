@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import kotlin.Int
import kotlin.String

@Entity
@Table(name = "exchange_info")
@DbComment("交易所信息")
public open class ExchangeInfo() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 10,
    unique = true,
  )
  public var id: Int = 0

  @DbComment("交易所代码")
  @Column(
    name = "exchange",
    nullable = true,
    length = 32,
    unique = true,
  )
  public var exchange: String? = null

  @DbComment("交易所码表的第一条记录对应的日期")
  @Column(
    name = "first_date",
    nullable = false,
    length = 10,
  )
  public var first_date: LocalDate? = null
}
