package sz.ebean.gen.dbinfo

import jodd.util.StringUtil
import java.sql.JDBCType
import java.sql.Types
import java.util.*
import kotlin.reflect.KClass

//
// Created by kk on 2021/5/5.
//
class ColumnInfo {
    var column_name = ""

    var jdbc_type = 0

    val jdbc_type_name: String
        get() {
            return JDBCType.valueOf(this.jdbc_type).name
        }

    var type_name = ""

    var column_size = 0

    var is_autoincrement = false

    var is_pk = false

    var null_able = true

    var default_value: String? = null

    var remarks = ""

    val field_name: String
        get() {
            return StringUtil.fromCamelCase(column_name, '_')
        }

    val kotlin_type_name: String
        get() {
            return kotlinType().qualifiedName!!
        }

    fun kotlinType(): KClass<*> {
        val kotlinType = kotlinTypeOf(JDBCType.valueOf(this.jdbc_type))
        if (this.is_pk && kotlinType == kotlin.String::class && this.column_size == 36) {
            // 主键, 且长度为 36, 字符串, 所以推断为 uuid
            return UUID::class
        }
        return kotlinType
    }


    fun isWhenCreated(): Boolean {
        if (this.field_name in setOf<String>(
                "created",
                "created_at",
                "when_created"
            ) && this.jdbc_type in setOf(Types.TIME, Types.TIMESTAMP)
        ) {
            return true
        }

        return false
    }

    fun isWhenModified(): Boolean {
        if (this.field_name in setOf(
                "modified",
                "modified_at",
                "when_modified",
                "updated",
                "updated_at",
                "when_updated"
            ) && this.jdbc_type in setOf(Types.TIME, Types.TIMESTAMP)
        ) {
            return true
        }

        return false
    }

    fun isVersion(): Boolean {
        if (this.field_name == "version" && this.jdbc_type == Types.BIGINT) {
            return true
        }

        return false
    }

    companion object {
        private val jdbcType2KotlinType = mapOf<JDBCType, KClass<*>>(
            JDBCType.BIT to kotlin.Boolean::class,
            JDBCType.TINYINT to kotlin.Short::class,
            JDBCType.SMALLINT to kotlin.Short::class,
            JDBCType.INTEGER to kotlin.Int::class,
            JDBCType.BIGINT to kotlin.Long::class,
            JDBCType.FLOAT to kotlin.Float::class,
            JDBCType.REAL to kotlin.Double::class,
            JDBCType.DOUBLE to kotlin.Double::class,
            JDBCType.NUMERIC to java.math.BigDecimal::class,
            JDBCType.DECIMAL to java.math.BigDecimal::class,
            JDBCType.CHAR to kotlin.String::class,
            JDBCType.VARCHAR to kotlin.String::class,
            JDBCType.LONGVARCHAR to kotlin.String::class,
            JDBCType.DATE to java.time.LocalDate::class,
            JDBCType.TIME to java.time.LocalTime::class,
            JDBCType.TIMESTAMP to java.time.LocalDateTime::class,
            JDBCType.BINARY to kotlin.ByteArray::class,
            JDBCType.VARBINARY to kotlin.ByteArray::class,
            JDBCType.LONGVARBINARY to kotlin.ByteArray::class,
            JDBCType.BLOB to kotlin.ByteArray::class,
            JDBCType.CLOB to kotlin.ByteArray::class,
            JDBCType.BOOLEAN to kotlin.Boolean::class,
            JDBCType.NCHAR to kotlin.String::class,
            JDBCType.NVARCHAR to kotlin.String::class,
            JDBCType.LONGNVARCHAR to kotlin.String::class,
            JDBCType.NCLOB to kotlin.ByteArray::class,
            JDBCType.SQLXML to kotlin.String::class
        )

        fun kotlinTypeOf(jdbcType: JDBCType): KClass<*> {
            return jdbcType2KotlinType.getOrDefault(jdbcType, kotlin.String::class)
        }
    }

}