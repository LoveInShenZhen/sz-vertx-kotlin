package sz.api.doc

import com.fasterxml.jackson.annotation.JsonIgnore
import sz.scaffold.Application
import sz.scaffold.annotations.Comment
import sz.scaffold.tools.console.PrettyTree
import sz.scaffold.tools.console.TreeNode
import sz.scaffold.tools.logger.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

//
// Created by kk on 17/8/24.
//

class FieldSchema {

    @Comment("在对象树里所处的层级")
    @JsonIgnore
    var level = 1

    @Comment("方法参数或者返回结果 Reply 中的字段名称")
    var name: String = ""

    @Comment("字段描述")
    var desc: String = ""

    @Comment("字段的Json数据类型")
    var type: String = ""

    @Comment("字段对应的kotlin类型")
    var kotlin_class: KClass<*>? = null

    @Comment("包含的字段, key: 字段名(name)")
    var fields: MutableMap<String, FieldSchema> = mutableMapOf()

    fun nodeText(): String {
        return "$name: $desc [$type]"
    }

    fun handleNode(fieldSchema: FieldSchema, node: TreeNode) {
        fieldSchema.fields.forEach { item ->
            val schema = item.value
            if (schema.fields.isEmpty()) {
                node.children.add(TreeNode(item.value.nodeText()))
            } else {
                val objNode = TreeNode(item.value.nodeText())
                node.children.add(objNode)
                handleNode(schema, objNode)
            }
        }
    }

    fun JsonSchema(): String {
        val root = TreeNode(nodeText())
        handleNode(this, root)
        val prettyTree = PrettyTree(root)
        return prettyTree.prettyTxt()
    }

    companion object {
        fun resolveFields(ownnerClass: KClass<*>, ownnerSchema: FieldSchema) {
            val maxLevel = 10
            ownnerClass.memberProperties.forEach {
                val propSchema = FieldSchema()
                propSchema.level = ownnerSchema.level + 1
                propSchema.name = it.name
                propSchema.desc = propertyDesc(it.annotations)
                propSchema.type = jsonType(it.returnType).typeName
                propSchema.kotlin_class = it.returnType.jvmErasure //it.returnType.javaType.typeName

                ownnerSchema.fields.put(propSchema.name, propSchema)

                if (isList(it.returnType) || isArray(it.returnType)) {
                    val elementKClass = listElementType(it.returnType).kotlin

                    val elementSchema = FieldSchema()
                    elementSchema.level = propSchema.level + 1
                    elementSchema.name = "element"
                    elementSchema.desc = "Element of List"
                    elementSchema.type = elementKClass.simpleName!!
                    elementSchema.kotlin_class = elementKClass //elementKClass.java.typeName

                    propSchema.fields.put(elementSchema.name, elementSchema)

                    if (elementSchema.level < maxLevel && isSimpleObject(elementKClass)) {
                        resolveFields(elementKClass, elementSchema)
                    }
                } else if (isMap(it.returnType)) {
                    val keyKClass = mapKeyType(it.returnType).kotlin
                    val keySchema = FieldSchema()
                    keySchema.level = propSchema.level + 1
                    keySchema.name = "key"
                    keySchema.desc = "Key of Map"
                    keySchema.type = keyKClass.simpleName!!
                    keySchema.kotlin_class = keyKClass //keyKClass.java.typeName

                    val valueKClass = mapValueType(it.returnType).kotlin
                    val valueSchema = FieldSchema()
                    valueSchema.level = propSchema.level + 1
                    valueSchema.name = "value"
                    valueSchema.desc = "Value of Map"
                    valueSchema.type = valueKClass.simpleName!!
                    valueSchema.kotlin_class = valueKClass //valueKClass.java.typeName

                    propSchema.fields.put(keySchema.name, keySchema)
                    propSchema.fields.put(valueSchema.name, valueSchema)

                    if (valueSchema.level < maxLevel && isSimpleObject(valueKClass)) {
                        resolveFields(valueKClass, valueSchema)
                    }
                } else if (isBasicType(it.returnType)) {
                    propSchema.fields.clear()
                } else {

                    resolveFields(propSchema.kotlin_class!!, propSchema)
                }
            }
        }

        private fun propertyDesc(annotations: List<Annotation>): String {
            val anno = annotations.find { it is Comment }
            if (anno != null) {
                return (anno as Comment).value
            }

            return ""
        }

    }
}
