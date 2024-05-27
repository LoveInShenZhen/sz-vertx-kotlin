@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName")

package models

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import io.ebean.`annotation`.WhenModified
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String

@Embeddable
public data class HistoryTickMetaUPK(
  @DbComment("证券合约代码,大写字母")
  @Column(
    name = "symbol",
    nullable = false,
    length = 32,
  )
  public var symbol: String = "",
  @DbComment("交易日期")
  @Column(
    name = "trade_date",
    nullable = false,
    length = 10,
  )
  public var trade_date: LocalDate? = null,
)

@Entity
@Table(name = "history_tick_meta")
@DbComment("历史 tick 行情数据文件分发元数据信息表")
public open class HistoryTickMeta() {
  @EmbeddedId
  public lateinit var historyTickMetaUPK: HistoryTickMetaUPK

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

  @DbComment("数据更新时间")
  @WhenModified
  public var updated_at: LocalDateTime? = null

  @DbComment("数据内容存储大小,单位:byte")
  @Column(
    name = "size",
    nullable = false,
    length = 10,
  )
  public var size: Int = 0

  @DbComment("数据内容的sha1值,小写字母")
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

  @DbComment("当分发方式不是OSS时,则在此字段里记录分发的细节信息,json文本格式")
  @Column(
    name = "other_dists_info",
    nullable = true,
    length = 65535,
  )
  public var other_dists_info: String? = null

  @DbComment("分发记录创建时间, 默认值: CURRENT_TIMESTAMP")
  @WhenCreated
  public var created_at: LocalDateTime? = null
}
