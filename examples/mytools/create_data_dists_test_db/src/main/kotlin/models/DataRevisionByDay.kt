@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "data_revision_by_day")
@DbComment("日频分发数据修订记录")
public open class DataRevisionByDay() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 19,
    unique = true,
  )
  public var id: Long = 0

  @DbComment("交易市场")
  @Column(
    name = "exchange",
    nullable = false,
    length = 32,
  )
  public var exchange: String = ""

  @DbComment("历史行情tick和bar数据的修订记录,需要记录对应的证券代码")
  @Column(
    name = "symbol",
    nullable = false,
    length = 64,
  )
  public var symbol: String = ""

  @DbComment("交易日期")
  @Column(
    name = "trade_date",
    nullable = false,
    length = 10,
  )
  public var trade_date: LocalDate? = null

  @DbComment("使用分发记录对应的表名称来作为分发数据的种类")
  @Column(
    name = "data_type",
    nullable = false,
    length = 64,
  )
  public var data_type: String = ""

  @DbComment("补丁数据的 oss bucket name")
  @Column(
    name = "oss_bucket",
    nullable = false,
    length = 32,
  )
  public var oss_bucket: String = ""

  @DbComment("补丁数据的 oss object path")
  @Column(
    name = "oss_object",
    nullable = false,
    length = 512,
  )
  public var oss_object: String = ""

  @DbComment("补丁数据大小, 单位: byte, 默认值: 0")
  @Column(
    name = "size",
    nullable = false,
    length = 10,
  )
  public var size: Int = 0

  @DbComment("补丁数据的sha1值, 小写字母")
  @Column(
    name = "sha1",
    nullable = false,
    length = 64,
  )
  public var sha1: String = ""

  @DbComment("分发方式,目前暂时只有: OSS, 默认值: OSS")
  @Column(
    name = "dists_type",
    nullable = false,
    length = 8,
  )
  public var dists_type: String? = null

  @DbComment("当分发方式不是OSS时,则在此字段里记录分发的详细信息,json文本格式")
  @Column(
    name = "other_dists_info",
    nullable = false,
    length = 65535,
  )
  public var other_dists_info: String = ""

  @DbComment("补丁数据记录里的最晚的时间戳")
  @Column(
    name = "last_cutime",
    nullable = false,
    length = 19,
  )
  public var last_cutime: LocalDateTime? = null

  @DbComment("修订记录创建时间")
  @WhenCreated
  public var created_at: LocalDateTime? = null

  @DbComment("补丁里的记录数")
  @Column(
    name = "records_count",
    nullable = false,
    length = 10,
  )
  public var records_count: Int = 0
}
