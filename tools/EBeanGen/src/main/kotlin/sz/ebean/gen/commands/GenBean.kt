package sz.ebean.gen.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.squareup.kotlinpoet.*
import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.Model
import io.ebean.annotation.DbComment
import io.ebean.annotation.View
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig
import jodd.io.FileUtil
import jodd.util.Wildcard
import sz.ebean.gen.dbinfo.*
import java.io.File
import java.math.BigDecimal
import java.util.*
import javax.persistence.*
import kotlin.reflect.full.isSubclassOf

//
// Created by kk on 2021/5/5.
//
class GenBean : CliktCommand(name = "gen") {

    val jdbcUrl by option(
        names = arrayOf("-d", "--jdbc"),
        help = "jdbc url, example: jdbc:mysql://localhost:3306/api_test?useSSL=false&useUnicode=true&characterEncoding=UTF-8"
    ).required()

    val user by option(
        names = arrayOf("-u", "--user"),
        help = "database access user name"
    ).required()

    val password by option(
        names = arrayOf("-p", "--password"),
        help = "database access password"
    ).required()

    val pkg by option(
        names = arrayOf("--pkg"),
        help = "package of the bean classes"
    ).default("models")

    val prefix by option(
        names = arrayOf("--prefix"),
        help = "bean class name prefix"
    ).default("")


    val outdir by option(
        names = arrayOf("-o", "--outdir"),
        help = "output dir. default is ./build dir"
    ).file(canBeFile = false).default(value = File("./build"), defaultForHelp = "./build")

    val excludes by option(
        names = arrayOf("-x", "--excludes"),
        help = "需要排除的表名称,支持通配符,多个名称之间使用逗号分隔"
    ).default("")

    val with_view by option(
        names = arrayOf("--with-view"),
        help = "是否包含视图"
    ).flag("--without-view", default = false)

    val use_utc_instant by option(
        names = arrayOf("--use-utc"),
        help = "数据库里Datetime使用UTC保存时, 使用此选项, 对应的字段的java类型为 Instant"
    ).flag("--use-localtime", default = false)

    var exclude_pattern: List<String> = listOf()

    private var dbinfo: DBInfo? = null

    override fun run() {
        if (use_utc_instant) {
            ColumnInfo.useUtcTime()
        } else {
            ColumnInfo.useLocalTime()
        }

        exclude_pattern = excludes.split(",")

        val db = newDatabase()

        var tableCommentService: TableCommentService? = null

        if (this.jdbcUrl.startsWith("jdbc:mysql:")) {
            // mysql
            tableCommentService = MysqlTableCommentService(db)
        }

        dbinfo = DBInfo(db, tableCommentService)
        val tables = mutableListOf<TableInfo>()
        tables.addAll(dbinfo!!.tables())

        if (with_view) {
            tables.addAll(dbinfo!!.views())
        }

        FileUtil.mkdirs(this.outdir)

        tables.forEach {
            it.prefix = this.prefix
            if (excludeTable(it.table_name).not()) {
                if (it.table_type == "VIEW") {
                    echo("为视图: ${it.table_name} 生成实体类代码: ${it.class_name}")
                } else {
                    echo("为表: ${it.table_name} 生成实体类代码: ${it.class_name}")
                }
                buildEntity(it, this.outdir.absolutePath)
            }
        }

        echo("完毕")
    }

    private fun wrapName(name : String) : String {
        return "`${name}`"
    }

    private fun excludeTable(tableName: String): Boolean {
        exclude_pattern.forEach { pattern ->
            if (Wildcard.match(tableName, pattern)) {
                return true
            }
        }

        return false
    }

    private fun newDatabase(): Database {
        val dataSourceConfig = DataSourceConfig()
        dataSourceConfig.username = user
        dataSourceConfig.password = password
        dataSourceConfig.url = jdbcUrl

        val config = DatabaseConfig()
        config.dataSourceConfig = dataSourceConfig

        return DatabaseFactory.create(config)
    }

    private fun buildEntity(tableInfo: TableInfo, destDir: String) {
        val pkCount = tableInfo.pk_columns.size
        if (pkCount > 1) {
            buildCompositePKEntity(tableInfo, destDir)
        } else {
            buildSinglePKEntity(tableInfo, destDir)
        }
    }

    private fun buildNormalField(tableInfo: TableInfo, columnInfo: ColumnInfo): PropertySpec {
        // 根据 columnInfo, 来确定实体类里, 该 field 的 类型, 是否可以为 null, 初始值
        val typeName: TypeName
        val initValue: Any?
        val fieldType = columnInfo.kotlinType()

        if (columnInfo.null_able) {
            // 字段在数据库表里允许为 null
            typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
            initValue = null
        } else {
            // 字段在数据库表里, 不允许为 null
            // 再判断, 在表结构定义里, 该 column 是否指定了默认值
            if (columnInfo.default_value.isNullOrBlank()) {
                // 表结构定义里, 没有指定默认值, 那么, 我们根据该字段的 kotlinType 来设置其java类型的默认值
                // 原生数值类型, 默认值为 0
                // BigDecimal 类型, 默认值为 0
                // String 类型, 默认值为 ""
                // Boolean 类型, 默认值为 False
                // Uuid 类型, 默认值为 全0 的uuid
                // 其他类型, 默认值为 null

                if (fieldType == BigDecimal::class) {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                    initValue = "BigDecimal.ZERO"
                } else if (fieldType.isSubclassOf(Number::class)) {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                    initValue = 0
                } else if (fieldType == String::class) {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                    initValue = ""
                } else if (fieldType == Boolean::class) {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                    initValue = false
                } else if (fieldType == UUID::class) {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                    initValue = "UUID.randomUUID()"
                } else {
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
                    initValue = null
                }
            } else {
                // 表结构定义里, 有指定默认值. 那么我们设置该实体类的 field 类型为 null able, 这样实体类不给此字段
                // 设置值时, insert 到表里, 会自动save 默认值
                typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
                initValue = null
            }
        }

        val builder = PropertySpec.builder(
            name = columnInfo.field_name,
            type = typeName
        ).mutable(true)

        if (fieldType == String::class) {
            builder.initializer("%S", initValue)
        } else {
            builder.initializer("%L", initValue)
        }

        if (columnInfo.is_pk && tableInfo.pk_columns.size == 1) {
            // 只有单主键的情况, 才使用 @Id 注解
            // 复合主键暂不支持
            builder.addAnnotation(Id::class)
        }

        val comments = mutableListOf<String>()
        if (columnInfo.remarks.isNotBlank()) {
            comments.add(columnInfo.remarks)
        }
        if (columnInfo.default_value.isNullOrBlank().not()) {
            comments.add("默认值: ${columnInfo.default_value!!}")
        }

        if (comments.size > 0) {
            builder.addAnnotation(
                AnnotationSpec.builder(DbComment::class)
                    .addMember("%S", comments.joinToString(", "))
                    .build()
            )
        }

        if (columnInfo.isWhenCreated()) {
            builder.addAnnotation(WhenCreated::class)
        } else if (columnInfo.isWhenModified()) {
            builder.addAnnotation(WhenModified::class)
        } else if (columnInfo.isVersion()) {
            builder.addAnnotation(Version::class)
        } else {
            val columnAnnSpecBuilder = AnnotationSpec.builder(Column::class)
            columnAnnSpecBuilder.addMember("name = %S", this.wrapName(columnInfo.field_name))

            if (columnInfo.null_able) {
                columnAnnSpecBuilder.addMember("nullable = true")
            } else {
                columnAnnSpecBuilder.addMember("nullable = false")
            }

            if (columnInfo.column_size > 0) {
                columnAnnSpecBuilder.addMember("length = ${columnInfo.column_size}")
            }

            if (tableInfo.isUnique(columnInfo.column_name)) {
                columnAnnSpecBuilder.addMember("unique = true")
            }

            builder.addAnnotation(columnAnnSpecBuilder.build())
        }

        return builder.build()
    }

    // 构建单主键的实体
    private fun buildSinglePKEntity(tableInfo: TableInfo, destDir: String) {
        val fileName = tableInfo.class_name
        val fileBuilder = FileSpec.builder(this.pkg, fileName)
            .suppressWarningTypes("RedundantVisibilityModifier", "MemberVisibilityCanBePrivate", "PropertyName", "unused")

        val entityClassBuilder = TypeSpec.classBuilder(tableInfo.class_name)
            .superclass(Model::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("dataSource", String::class)
                            .defaultValue("%S", "")
                            .build()
                    ).build()
            )
            .addSuperclassConstructorParameter("dataSource")
            .addModifiers(KModifier.OPEN)
            .addAnnotation(MappedSuperclass::class)
            .addAnnotation(Entity::class.java)


        if (tableInfo.table_type == "VIEW") {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(View::class)
                    .addMember("name = %S", this.wrapName(tableInfo.table_name))
                    .build()
            )
        } else {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(Table::class)
                    .addMember("name = %S", this.wrapName(tableInfo.table_name))
                    .build()
            )
        }

        if (tableInfo.comment.isNotBlank()) {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(DbComment::class)
                    .addMember("%S", tableInfo.comment)
                    .build()
            )
        }


        tableInfo.columns.forEach { columnInfo ->
            val propSpec = buildNormalField(tableInfo, columnInfo)
            entityClassBuilder.addProperty(propSpec)
        }

        fileBuilder.addType(entityClassBuilder.build())

        fileBuilder.build().writeTo(File(destDir))
    }

    private fun buildCompositePKClass(tableInfo: TableInfo): TypeSpec {
        val pkClassName = "${tableInfo.class_name}UPK"
        val pkClassBuilder = TypeSpec.classBuilder(pkClassName)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Embeddable::class.java)

        val pkClassConstructorBuilder = FunSpec.constructorBuilder()
        tableInfo.pkColumnInfos().forEach { columnInfo ->
            var typeName : TypeName
            var defaultValue : Any?
            if (columnInfo.null_able) {
                typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
                defaultValue = null
            } else {
                // 字段在数据库表里, 不允许为 null
                // 再判断, 在表结构定义里, 该 column 是否指定了默认值
                if (columnInfo.default_value.isNullOrBlank()) {
                    // 表结构定义里, 没有指定默认值, 那么, 我们根据该字段的 kotlinType 来设置其java类型的默认值
                    // 原生数值类型, 默认值为 0
                    // BigDecimal 类型, 默认值为 0
                    // String 类型, 默认值为 ""
                    // Boolean 类型, 默认值为 False
                    // Uuid 类型, 默认值为 全0 的uuid
                    // 其他类型, 默认值为 null

                    if (columnInfo.kotlinType() == BigDecimal::class) {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                        defaultValue = "BigDecimal.ZERO"
                    } else if (columnInfo.kotlinType().isSubclassOf(Number::class)) {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                        defaultValue = 0
                    } else if (columnInfo.kotlinType() == String::class) {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                        defaultValue = ""
                    } else if (columnInfo.kotlinType() == Boolean::class) {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                        defaultValue = false
                    } else if (columnInfo.kotlinType() == UUID::class) {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = false)
                        defaultValue = "UUID.randomUUID()"
                    } else {
                        typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
                        defaultValue = null
                    }
                } else {
                    // 表结构定义里, 有指定默认值. 那么我们设置该实体类的 field 类型为 null able, 这样实体类不给此字段
                    // 设置值时, insert 到表里, 会自动save 默认值
                    typeName = columnInfo.kotlinType().asTypeName().copy(nullable = true)
                    defaultValue = null
                }
            }

            val paramSpecBuilder = ParameterSpec.builder(name = columnInfo.field_name, type = typeName)
            if (columnInfo.kotlinType() == String::class) {
                paramSpecBuilder.defaultValue("%S", defaultValue)
            } else {
                paramSpecBuilder.defaultValue("%L", defaultValue)
            }


            val comments = mutableListOf<String>()
            if (columnInfo.remarks.isNotBlank()) {
                comments.add(columnInfo.remarks)
            }
            if (columnInfo.default_value.isNullOrBlank().not()) {
                comments.add("默认值: ${columnInfo.default_value!!}")
            }

            if (comments.size > 0) {
                paramSpecBuilder.addAnnotation(
                    AnnotationSpec.builder(DbComment::class)
                        .addMember("%S", comments.joinToString(", "))
                        .build()
                )
            }

            val columnAnnSpecBuilder = AnnotationSpec.builder(Column::class)
            columnAnnSpecBuilder.addMember("name = %S", this.wrapName(columnInfo.field_name))

            if (columnInfo.null_able) {
                columnAnnSpecBuilder.addMember("nullable = true")
            } else {
                columnAnnSpecBuilder.addMember("nullable = false")
            }

            if (columnInfo.column_size > 0) {
                columnAnnSpecBuilder.addMember("length = ${columnInfo.column_size}")
            }
            paramSpecBuilder.addAnnotation(columnAnnSpecBuilder.build())

            val propertyBuilder = PropertySpec.builder(name = columnInfo.field_name, type = typeName).mutable(true)
                .initializer(columnInfo.field_name)

            pkClassConstructorBuilder.addParameter(paramSpecBuilder.build())

            pkClassBuilder.primaryConstructor(pkClassConstructorBuilder.build()).addProperty(propertyBuilder.build())
        }

        return pkClassBuilder.build()
    }

    private fun buildCompositePKEntity(tableInfo: TableInfo, destDir: String) {
        val fileName = tableInfo.class_name
        val fileBuilder = FileSpec.builder(this.pkg, fileName)
            .suppressWarningTypes("RedundantVisibilityModifier", "MemberVisibilityCanBePrivate", "PropertyName")


        fileBuilder.addType(buildCompositePKClass(tableInfo))

        val entityClassBuilder = TypeSpec.classBuilder(tableInfo.class_name)
            .superclass(Model::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("dataSource", String::class)
                            .defaultValue("%S", "")
                            .build()
                    ).build()
            )
            .addSuperclassConstructorParameter("dataSource")
            .addModifiers(KModifier.OPEN)
            .addAnnotation(MappedSuperclass::class)
            .addAnnotation(Entity::class.java)


        if (tableInfo.table_type == "VIEW") {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(View::class)
                    .addMember("name = %S", tableInfo.table_name)
                    .build()
            )
        } else {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(Table::class)
                    .addMember("name = %S", tableInfo.table_name)
                    .build()
            )
        }

        if (tableInfo.comment.isNotBlank()) {
            entityClassBuilder.addAnnotation(
                AnnotationSpec.builder(DbComment::class)
                    .addMember("%S", tableInfo.comment)
                    .build()
            )
        }

        // 复合主键 Property
        val upkProperty = PropertySpec.builder(
            name = "${tableInfo.class_name}UPK".replaceFirstChar { it.lowercase(Locale.getDefault()) },
            type = ClassName(this.pkg, "${tableInfo.class_name}UPK")
        ).mutable(true)
            .addModifiers(KModifier.LATEINIT)
            .addAnnotation(EmbeddedId::class.java)
            .build()

        entityClassBuilder.addProperty(upkProperty)

        tableInfo.columns.filter { it.is_pk.not() }.forEach { columnInfo ->
            val propSpec = buildNormalField(tableInfo, columnInfo)
            entityClassBuilder.addProperty(propSpec)
        }

        fileBuilder.addType(entityClassBuilder.build())

        fileBuilder.build().writeTo(File(destDir))
    }

}

internal fun FileSpec.Builder.suppressWarningTypes(vararg types: String): FileSpec.Builder {
    if (types.isEmpty()) {
        return this
    }

    val format = "%S,".repeat(types.count()).trimEnd(',')
    addAnnotation(
        AnnotationSpec.builder(ClassName("", "Suppress"))
            .addMember(format, *types)
            .build()
    )
    return this
}

