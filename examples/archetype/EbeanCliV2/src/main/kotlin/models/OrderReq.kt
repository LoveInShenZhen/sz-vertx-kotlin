@file:Suppress("RedundantVisibilityModifier","MemberVisibilityCanBePrivate","PropertyName","unused")

package models

import com.fasterxml.jackson.databind.node.ArrayNode
import io.ebean.`annotation`.DbComment
import io.ebean.`annotation`.DbJson
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kotlin.Long
import kotlin.String

@Entity
@Table(name = "order_req")
@DbComment("报单请求")
public open class OrderReq() {
  @Id
  @Column(
    name = "id",
    nullable = false,
    length = 19,
    unique = true,
  )
  public var id: Long = 0

  @DbComment("本地生成的报单请求id")
  @Column(
    name = "request_id",
    nullable = false,
    length = 64,
    unique = true,
  )
  public var request_id: String = ""

  @DbComment("持仓tag, json array")
  @Column(
    name = "posi_tag",
    nullable = true,
    length = 1073741824,
  )
  @DbJson
  public var posi_tag: ArrayNode? = null

  @DbComment("策略名称")
  @Column(
    name = "strategy_name",
    nullable = false,
    length = 64,
  )
  public var strategy_name: String = ""

  @DbComment("委托备注")
  @Column(
    name = "order_remark",
    nullable = false,
    length = 200,
  )
  public var order_remark: String = ""
}
