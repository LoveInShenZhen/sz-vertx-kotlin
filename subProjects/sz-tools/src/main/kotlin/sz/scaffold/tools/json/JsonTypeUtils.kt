package sz.scaffold.tools.json

import jodd.datetime.JDateTime
import jodd.util.ClassUtil
import org.apache.commons.lang3.reflect.TypeUtils
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

//
// Created by kk on 17/8/24.
//


enum class JsonDataType(val typeName: String) {
    BOOLEAN("Boolean"),
    NUMBER("Number"),
    STRING("String"),
    DATETIME("DateTime String"),
    MAP("Map"),
    LIST("List"),
    OBJECT("Object")
}

fun jsonType(kType: KType): JsonDataType {

    return when {
        isBoolean(kType) -> JsonDataType.BOOLEAN
        isNumber(kType) -> JsonDataType.NUMBER
        isDateTime(kType) -> JsonDataType.DATETIME
        isString(kType) -> JsonDataType.STRING
        isMap(kType) -> JsonDataType.MAP
        isList(kType) -> JsonDataType.LIST
        isArray(kType) -> JsonDataType.LIST
        else -> JsonDataType.OBJECT
    }
}

fun isOneOfTypes(lookupClass: Class<*>, vararg targetClasses: Class<*>): Boolean {
    for (tclass in targetClasses) {
        if (ClassUtil.isTypeOf(lookupClass, tclass)) {
            return true
        }
    }

    return false
}

fun isBoolean(kType: KType): Boolean {
    if (kType == Boolean::class) {
        return true
    }
    val rawType = ClassUtil.getRawType(kType.javaType)
    if (rawType == Boolean::class.java) {
        return true
    }
    return false
}

fun isNumber(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)

    return isOneOfTypes(rawType,
        Byte::class.java,
        Short::class.java,
        Byte::class.java,
        Int::class.java,
        Long::class.java,
        Float::class.java,
        Double::class.java,
        Number::class.java,
        BigDecimal::class.java)
}

fun isString(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)

    if (isOneOfTypes(rawType,
            String::class.java,
            CharSequence::class.java)) {
        return true
    }

    if (isDateTime(kType)) {
        return true
    }

    return false
}

fun isDateTime(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return isOneOfTypes(rawType,
        LocalDate::class.java,
        LocalDateTime::class.java,
        LocalTime::class.java,
        Date::class.java,
        java.sql.Date::class.java,
        Calendar::class.java,
        jodd.datetime.JDateTime::class.java)
}

fun isBasicType(kClass: KClass<*>): Boolean {
    return isOneOfTypes(kClass.javaObjectType,
        Byte::class.java,
        java.lang.Byte::class.java,
        Short::class.java,
        java.lang.Short::class.java,
        Int::class.java,
        java.lang.Integer::class.java,
        Long::class.java,
        java.lang.Long::class.java,
        Float::class.java,
        java.lang.Float::class.java,
        Double::class.java,
        java.lang.Double::class.java,
        Number::class.java,
        BigDecimal::class.java,
        Boolean::class.java,
        java.lang.Boolean::class.java,
        CharSequence::class.java,
        LocalDate::class.java,
        LocalDateTime::class.java,
        LocalTime::class.java,
        Date::class.java,
        java.sql.Date::class.java,
        JDateTime::class.java,
        Calendar::class.java)
}

fun isBasicType(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return isOneOfTypes(rawType,
        Byte::class.java,
        java.lang.Byte::class.java,
        Short::class.java,
        java.lang.Short::class.java,
        Int::class.java,
        java.lang.Integer::class.java,
        Long::class.java,
        java.lang.Long::class.java,
        Float::class.java,
        java.lang.Float::class.java,
        Double::class.java,
        java.lang.Double::class.java,
        Number::class.java,
        BigDecimal::class.java,
        Boolean::class.java,
        java.lang.Boolean::class.java,
        CharSequence::class.java,
        LocalDate::class.java,
        LocalDateTime::class.java,
        LocalTime::class.java,
        Date::class.java,
        java.sql.Date::class.java,
        JDateTime::class.java,
        Calendar::class.java)
}

fun isContainerType(kClass: KClass<*>): Boolean {
    if (TypeUtils.isArrayType(kClass.javaObjectType)) {
        return true
    }

    return isOneOfTypes(kClass.javaObjectType,
        kotlin.collections.Map::class.java,
        kotlin.collections.Set::class.java,
        kotlin.collections.List::class.java)
}

fun isSimpleObject(kClass: KClass<*>): Boolean {
    return when {
        isBasicType(kClass) -> false
        isContainerType(kClass) -> false
        else -> true
    }
}

fun isMap(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return isOneOfTypes(rawType, kotlin.collections.Map::class.java)
}

fun mapKeyType(kType: KType): Class<*> {
//    val rawType = ClassUtil.getRawType(kType.javaType)
    return ClassUtil.getComponentType(kType.javaType, 0)
}

fun mapValueType(kType: KType): Class<*> {
//    val rawType = ClassUtil.getRawType(kType.javaType)
    return ClassUtil.getComponentType(kType.javaType, 1)
}

fun isList(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return isOneOfTypes(rawType,
        kotlin.collections.List::class.java,
        kotlin.collections.Set::class.java)
}

fun isArray(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return TypeUtils.isArrayType(rawType)
}

fun listElementType(kType: KType): Class<*> {
    return ClassUtil.getComponentType(kType.javaType, 0)
}