@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenModified
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "trade_calendar_meta")
@DbComment("交易日历分发记录")
public open class TradeCalendarMeta() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 19,
    unique = true,
  )
  public var id: Long = 0

  @DbComment("OSS Bucket Name")
  @Column(
    name = "oss_bucket",
    nullable = false,
    length = 32,
  )
  public var oss_bucket: String = ""

  @DbComment("OSS Object path")
  @Column(
    name = "oss_object",
    nullable = false,
    length = 512,
  )
  public var oss_object: String = ""

  @DbComment("数据内容存储大小, 单位:byte")
  @Column(
    name = "size",
    nullable = false,
    length = 10,
  )
  public var size: Int = 0

  @DbComment("数据内容的sha1值, 小写字母")
  @Column(
    name = "sha1",
    nullable = false,
    length = 64,
  )
  public var sha1: String = ""

  @DbComment("分发方式,目前暂时只有: OSS")
  @Column(
    name = "dists_type",
    nullable = false,
    length = 8,
  )
  public var dists_type: String = ""

  @DbComment("当分发方式不是OSS时,则在此字段里记录分发的详细信息,json文本格式")
  @Column(
    name = "other_dists_info",
    nullable = true,
    length = 65535,
  )
  public var other_dists_info: String? = null

  @DbComment("数据更新时间")
  @WhenModified
  public var updated_at: LocalDateTime? = null
}
