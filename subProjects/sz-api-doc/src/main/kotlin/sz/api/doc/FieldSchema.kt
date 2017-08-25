package sz.api.doc

import com.fasterxml.jackson.annotation.JsonIgnore
import sz.scaffold.annotations.Comment
import sz.scaffold.tools.json.toJsonPretty
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

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

    @Comment("包含的字段, key: 字段名(name)")
    var fields: MutableMap<String, FieldSchema>? = mutableMapOf()

    fun JsonSchema():String {
        return this.toJsonPretty()
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

                ownnerSchema.fields!!.put(propSchema.name, propSchema)

                if (isList(it.returnType) || isArray(it.returnType)) {
                    val elementKClass = listElementType(it.returnType).kotlin

                    val elementSchema = FieldSchema()
                    elementSchema.level = propSchema.level + 1
                    elementSchema.name = "element"
                    elementSchema.desc = "Element of List (or Array)"
                    elementSchema.type = elementKClass.simpleName!!

                    propSchema.fields!!.put(elementSchema.name, elementSchema)

                    if (elementSchema.level < maxLevel && isSimpleObject(elementKClass)) {
                        resolveFields(elementKClass, elementSchema)
                    }
                }

                if (isMap(it.returnType)) {
                    val keyKClass = mapKeyType(it.returnType).kotlin
                    val keySchema = FieldSchema()
                    keySchema.level = propSchema.level + 1
                    keySchema.name = "key"
                    keySchema.desc = "Key of Map"
                    keySchema.type = keyKClass.simpleName!!

                    val valueKClass = mapValueType(it.returnType).kotlin
                    val valueSchema = FieldSchema()
                    valueSchema.level = propSchema.level + 1
                    valueSchema.name = "value"
                    valueSchema.desc = "Value of Map"
                    valueSchema.type = valueKClass.simpleName!!

                    propSchema.fields!!.put(keySchema.name, keySchema)
                    propSchema.fields!!.put(valueSchema.name, valueSchema)

                    if (valueSchema.level < maxLevel && isSimpleObject(valueKClass)) {
                        resolveFields(valueKClass, valueSchema)
                    }
                }

                if (isBasicType(it.returnType)) {
                    propSchema.fields = null
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
