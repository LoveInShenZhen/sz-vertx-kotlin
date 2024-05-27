@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import io.ebean.`annotation`.WhenModified
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.Boolean
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "err_log")
@DbComment("错误记录")
public open class ErrLog() {
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

  @DbComment("数据类型: history-tick, history-bar")
  @Column(
    name = "data_type",
    nullable = false,
    length = 32,
  )
  public var data_type: String = ""

  @DbComment("错误类别")
  @Column(
    name = "err_type",
    nullable = false,
    length = 32,
  )
  public var err_type: String = ""

  @Column(
    name = "err_msg",
    nullable = false,
    length = 65535,
  )
  public var err_msg: String = ""

  @DbComment("错误是否已经被修正, 默认值: 0")
  @Column(
    name = "fixed",
    nullable = false,
    length = 1,
  )
  public var fixed: Boolean = false

  @DbComment("是否忽略此错误, 默认值: 0")
  @Column(
    name = "ignored",
    nullable = false,
    length = 1,
  )
  public var ignored: Boolean = false

  @WhenCreated
  public var created_at: LocalDateTime? = null

  @WhenModified
  public var updated_at: LocalDateTime? = null
}
