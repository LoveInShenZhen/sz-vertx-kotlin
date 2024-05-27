@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.WhenCreated
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import kotlin.Int
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "instrument_last_snapshot_meta")
@DbComment("码表数据快照的分发元数据")
public open class InstrumentLastSnapshotMeta() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 19,
    unique = true,
  )
  public var id: Long = 0

  @DbComment("交易所代码")
  @Column(
    name = "exchange",
    nullable = true,
    length = 16,
  )
  public var exchange: String? = null

  @DbComment("快照唯一id")
  @Column(
    name = "snapshot_id",
    nullable = false,
    length = 64,
  )
  public var snapshot_id: String = ""

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
    nullable = true,
    length = 65535,
  )
  public var other_dists_info: String? = null

  @DbComment("快照创建时间")
  @WhenCreated
  public var created_at: LocalDateTime? = null
}
