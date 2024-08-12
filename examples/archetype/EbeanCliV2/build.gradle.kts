import java.nio.file.Path
import org.apache.commons.io.FileUtils

buildscript {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/public/")
        }
        mavenLocal()
    }

    dependencies {
        classpath(group="commons-io", name="commons-io", version="2.16.1")
    }
}


plugins {
    kotlin("jvm") version "2.0.0"
    id("org.beryx.runtime") version "1.12.7"
    id("io.ebean").version("15.5.0")
    id("org.jetbrains.kotlin.kapt") version "2.0.0"
    application
}

group = "com.kts"
version = "1.0-SNAPSHOT"


repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("com.github.ajalt.clikt:clikt:4.3.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("io.github.config4k:config4k:0.7.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:8.4.0")

    implementation("io.ebean:ebean:15.5.0")


    implementation("io.ebean:ebean-ddl-generator:15.5.0")
    kapt("io.ebean:querybean-generator:15.5.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
//    implementation("org.jodd:jodd-util:6.1.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
//    implementation("org.apache.commons:commons-lang3:3.14.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
//    implementation("org.apache.commons:commons-csv:1.11.0")
    // https://mvnrepository.com/artifact/commons-io/commons-io
//    implementation("commons-io:commons-io:2.16.1")



    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin { // Extension for easy setup
    jvmToolchain(17) // Target version of generated JVM bytecode. See 7️⃣
}

tasks.withType<JavaExec> {
    val appconf = Path.of(this.project.rootDir.absolutePath, "conf", "application.conf")
    val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")
    val ebeanConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "ebean.yml")
    this.jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8",
        "-Dlogback.configurationFile=${logbackConfPath}",
        "-Dprops.file=${ebeanConfPath}",
        "-Dconfig.file=${appconf}"
    )
}

application {
    mainClass.set("kts.MainApp")
}

tasks.withType<CreateStartScripts> {

    this.doLast {
        val unix_script_lines = unixScript.readLines()
        val new_unix_script_lines = StringBuilder()
        unix_script_lines.forEach { line ->
            if (line.startsWith("DEFAULT_JVM_OPTS=")) {
                new_unix_script_lines.appendLine("DEFAULT_JVM_OPTS=\"-Dlogback.configurationFile=\$APP_HOME/conf/logback.xml -Dprops.file=\$APP_HOME/conf/ebean.yml -Dconfig.file=\$APP_HOME/conf/application.conf\"")
            } else {
                new_unix_script_lines.appendLine(line)
            }
        }

        unixScript.writeText(new_unix_script_lines.toString())

        val win_script_lines = windowsScript.readLines()
        val new_win_script_lines = StringBuilder()
        win_script_lines.forEach { line ->
            if (line.startsWith("set DEFAULT_JVM_OPTS=")) {
                new_win_script_lines.appendLine("set DEFAULT_JVM_OPTS=-Dlogback.configurationFile=%APP_HOME%\\conf\\logback.xml -Dprops.file=%APP_HOME%\\conf\\ebean.yml -Dconfig.file=%APP_HOME%\\conf\\application.conf")
            } else {
                new_win_script_lines.appendLine(line)
            }
        }
        windowsScript.writeText(new_win_script_lines.toString())
    }
}

fun currentOsType() : String {
    val os_name = System.getProperty("os.name").lowercase()
    if (os_name.contains("win")) {
        return "win"
    }
    if (os_name.contains("linux")) {
        return "linux"
    }
    if (os_name.contains("mac")) {
        return "mac"
    }

    return "unknown"
}

fun targetOsType(): String {
    val targetOs = System.getProperty("target.os")
    if (targetOs.isNullOrBlank()) {
        return currentOsType()
    } else {
        return targetOs
    }
}

fun jdkHome(): String {
    val os_type = currentOsType()
    val target_os_type = targetOsType()
    if (os_type == target_os_type) {
        val java_home = System.getenv("JAVA_HOME")
        if (java_home.isNullOrBlank()) {
            println("请设置 JAVA_HOME 环境变量")
            throw Exception("请设置 JAVA_HOME 环境变量")
        }
        return java_home
    } else {
        val jdk_home_env_name = "${target_os_type.uppercase()}_JDK_HOME"
        val jdk_home = System.getenv(jdk_home_env_name)
        if (jdk_home.isNullOrBlank()) {
            println("请设置 $jdk_home_env_name 环境变量")
            throw Exception("请设置 $jdk_home_env_name 环境变量")
        }

        return jdk_home
    }
}

runtime {
    this.options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    if (targetOsType() == "win") {
        targetPlatform("win") {
            setJdkHome(jdkHome())
            addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
        }
    }

    if (targetOsType() == "linux") {
        targetPlatform("linux") {
            val linux_jdk_home = System.getenv("LINUX_JDK_HOME")
            if (linux_jdk_home.isNullOrBlank()) {
                println("请设置 LINUX_JDK_HOME 环境变量")
                throw Exception("请设置 LINUX_JDK_HOME 环境变量")
            }
            setJdkHome(linux_jdk_home)
            addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
        }
    }

    this.imageZip.set(layout.buildDirectory.file("runtime_dists/${project.name}-${targetOsType()}.zip").get().asFile)
}

tasks.named("runtime") {
    doLast {
        val src_dir = layout.projectDirectory.dir("conf").asFile
        val dest_dir = layout.buildDirectory.dir("image/${project.name}-${targetOsType()}/conf").get().asFile

        FileUtils.copyDirectory(src_dir, dest_dir)
    }
}

kapt {
    generateStubs = true
}

ebean {
    debugLevel = 1
    queryBeans = true
    kotlin = true
}


sourceSets {
    main {
        java.srcDirs.add(file("${layout.buildDirectory.asFile.get().path}/generated/source/kapt/main"))
    }
}

val distZip: Zip by tasks
distZip.apply {
    enabled = true
    into("${project.name}-${project.version}") {
        from(".").include("conf/**")
    }
}

// 这里展示了2种修改 task 配置的方法
tasks.named<Tar>("distTar") {
    enabled = false
    into("${project.name}-${project.version}") {
        from(".").include("conf/**")
    }
}
