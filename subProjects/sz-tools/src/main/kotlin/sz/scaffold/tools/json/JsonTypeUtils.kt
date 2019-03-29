package sz.scaffold.tools.json

import jodd.datetime.JDateTime
import jodd.util.ClassUtil
import org.apache.commons.lang3.reflect.TypeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

/**
 * Created by kk on 16/9/7.
 */

enum class JsonDataType(val typeName: String) {
    NUMBER("Number"),
    STRING("String"),
    DATETIME("DateTime String"),
    MAP("Map"),
    LIST("List"),
    OBJECT("Object")
}

fun jsonType(kType: KType): JsonDataType {

    return when {
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
    return targetClasses.any { ClassUtil.isTypeOf(lookupClass, it) }
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
            Number::class.java)
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
            JDateTime::class.java)
}

fun isBasicType(kClass: KClass<*>): Boolean {
    return isOneOfTypes(kClass.javaObjectType,
            Byte::class.java,
            Short::class.java,
            Byte::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            Number::class.java,
            CharSequence::class.java,
            LocalDate::class.java,
            LocalDateTime::class.java,
            LocalTime::class.java,
            Date::class.java,
            java.sql.Date::class.java,
            Calendar::class.java)
}

fun isBasicType(kType: KType):Boolean{
    val rawType = ClassUtil.getRawType(kType.javaType)
    return isOneOfTypes(rawType,
            Int::class.java,
            Number::class.java,
            CharSequence::class.java,
            LocalDate::class.java,
            LocalDateTime::class.java,
            LocalTime::class.java,
            Date::class.java,
            java.sql.Date::class.java,
            Calendar::class.java)
}

fun isContainerType(kClass: KClass<*>): Boolean {
    if (TypeUtils.isArrayType(kClass.javaObjectType)) {
        return true
    }

    return isOneOfTypes(kClass.javaObjectType,
            Map::class.java,
            Set::class.java,
            List::class.java)
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
    return isOneOfTypes(rawType, Map::class.java)
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
            List::class.java,
            Set::class.java)
}

fun isArray(kType: KType): Boolean {
    val rawType = ClassUtil.getRawType(kType.javaType)
    return TypeUtils.isArrayType(rawType)
}

fun listElementType(kType: KType): Class<*> {
    return ClassUtil.getComponentType(kType.javaType, 0)
}




