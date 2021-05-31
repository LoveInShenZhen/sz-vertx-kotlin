package sz.ebean.gen.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
import sz.ebean.gen.dbinfo.ColumnInfo
import sz.ebean.gen.dbinfo.DBInfo
import sz.ebean.gen.dbinfo.TableInfo
import java.io.File
import java.lang.StringBuilder
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

    val output by option(
        names = arrayOf("-o", "--output"),
        help = "output dir. default is ./build dir"
    ).default("./build")

    override fun run() {
        val db = newDatabase()

//        db.beginTransaction().use { tran ->
//            val cnn = tran.connection
//            val metaData = cnn.metaData
//
//            val rs = metaData.getPrimaryKeys(cnn.catalog, null, "data_sdk_version")
//            val count = rs.metaData.columnCount
//            for (i in 1..count) {
//
//                println("ColumnNam : ${rs.metaData.getColumnName(i)}")
//                println("colunm type: ${rs.metaData.getColumnTypeName(i)}")
//            }
//            while (rs.next()) {
//                println("PK_NAME: ${rs.getString("PK_NAME")}")
//                println("COLUMN_NAME: ${rs.getString("COLUMN_NAME")}")
//                println("KEY_SEQ: ${rs.getString("KEY_SEQ")}")
//            }
//        }


        val dbinfo = DBInfo(db)
        val tables = dbinfo.tables()
        println(tables.find { it.table_name == "data_user_base" })

        val destDir = outputDirPath()
        FileUtil.mkdirs(destDir)

        tables.find { it.table_name == "data_authtoken" }.run {
            buildEntity(this!!, destDir)
        }

//        test()

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

    private fun outputDirPath(): String {
        var pkgDirs = this.pkg.replace(".", File.pathSeparator, true)
        val path = FileNameUtil.concat(this.output, pkgDirs)
        return path
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

        fileBuilder.build().writeTo(System.out)
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

    private fun test() {
        val greeterClass = ClassName("", "Greeter")
        val file = FileSpec.builder("", "HelloWorld")
            .addType(
                TypeSpec.classBuilder("Greeter")
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("name", String::class)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("name", String::class)
                            .initializer("name")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("greet")
                            .addStatement("println(%P)", "Hello, \$name")
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("main")
                    .addParameter("args", String::class, KModifier.VARARG)
                    .addStatement("%T(args[0]).greet()", greeterClass)
                    .build()
            )
            .suppressWarningTypes("edundantVisibilityModifier")
            .build()

        file.writeTo(System.out)
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