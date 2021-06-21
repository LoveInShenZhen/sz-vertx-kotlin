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
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig
import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.util.Wildcard
import sz.ebean.gen.dbinfo.ColumnInfo
import sz.ebean.gen.dbinfo.DBInfo
import sz.ebean.gen.dbinfo.TableInfo
import java.io.File
import javax.persistence.*

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

    val outdir by option(
        names = arrayOf("-o", "--outdir"),
        help = "output dir. default is ./build dir"
    ).file(canBeFile = false).default(value = File("./build"), defaultForHelp = "./build")

    val excludes by option(
        names = arrayOf("-x", "--excludes"),
        help = "需要排除的表名称,支持通配符,多个名称之间使用逗号分隔"
    ).default("")

    val with_view by option(
        help = "是否包含视图"
    ).flag("--with-view", default = false)

    var exclude_pattern: List<String> = listOf()

    override fun run() {
        exclude_pattern = excludes.split(",")

        val db = newDatabase()

        val dbinfo = DBInfo(db)
        val tables = mutableListOf<TableInfo>()
        tables.addAll(dbinfo.tables())

        if (with_view) {
            tables.addAll(dbinfo.views())
        }

        FileUtil.mkdirs(this.outdir)

        tables.forEach {
            if (excludeTable(it.table_name).not()) {
                echo("为表: ${it.table_name} 生成实体类代码: ${it.class_name}")
                buildEntity(it, this.outdir.absolutePath)
            }
        }

        echo("完毕")
    }

    private fun excludeTable(tableName:String):Boolean{
        exclude_pattern.forEach {pattern ->
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
        val fileName = tableInfo.class_name
        val fileBuilder = FileSpec.builder(this.pkg, fileName)
            .suppressWarningTypes("RedundantVisibilityModifier", "MemberVisibilityCanBePrivate", "PropertyName")

        val classBuilder = TypeSpec.classBuilder(tableInfo.class_name)
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
            .addAnnotation(
                AnnotationSpec.builder(Table::class)
                    .addMember("name = %S", tableInfo.table_name)
                    .build()
            )


        tableInfo.columns.forEach { columnInfo ->
            val propSpec = buildField(tableInfo, columnInfo)
            classBuilder.addProperty(propSpec)
        }

        fileBuilder.addType(classBuilder.build())

        fileBuilder.build().writeTo(File(destDir))
    }

    private fun buildField(tableInfo: TableInfo, columnInfo: ColumnInfo): PropertySpec {
        val builder = PropertySpec.builder(
            name = columnInfo.field_name,
            type = columnInfo.kotlinType().asTypeName().copy(nullable = columnInfo.null_able)
        )
            .mutable(true)
        if (columnInfo.null_able) {
            builder.initializer("null")
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

        if (columnInfo.is_pk) {
            builder.addAnnotation(Id::class)
        }

        if (columnInfo.isWhenCreated()) {
            builder.addAnnotation(WhenCreated::class)
        } else if (columnInfo.isWhenModified()) {
            builder.addAnnotation(WhenModified::class)
        } else if (columnInfo.isVersion()) {
            builder.addAnnotation(Version::class)
        } else {
            val columnAnnSpecBuilder = AnnotationSpec.builder(Column::class)
            columnAnnSpecBuilder.addMember("nullable = %S", columnInfo.null_able)
            if (columnInfo.column_size > 0) {
                columnAnnSpecBuilder.addMember("length = %S", columnInfo.column_size)
            }

            if (tableInfo.isUnique(columnInfo.column_name)) {
                columnAnnSpecBuilder.addMember("unique = %S", true)
            }

            builder.addAnnotation(columnAnnSpecBuilder.build())
        }

        return builder.build()
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