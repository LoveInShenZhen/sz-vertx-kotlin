import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path


plugins {
    kotlin("jvm") version "1.9.20"
    id("org.beryx.runtime") version "1.12.7"
    id("io.ebean").version("13.6.5")
    id("org.jetbrains.kotlin.kapt") version "1.9.20"
    id("com.github.johnrengelman.shadow") version("7.1.2")
    application
}

group = "com.myquant"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
//    maven {
//        url = uri("https://maven.aliyun.com/repository/public/")
//    }
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.github.ajalt.clikt:clikt:3.4.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("io.github.config4k:config4k:0.4.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("mysql:mysql-connector-java:8.0.29")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
    implementation("org.apache.commons:commons-csv:1.10.0")


    implementation("io.ebean:ebean:13.25.0")
//    implementation("io.ebean:ebean-ddl-generator:13.6.4")
    kapt("io.ebean:querybean-generator:13.25.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.+")
    implementation("org.jodd:jodd-util:6.1.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaExec> {
    val appconf = Path.of(this.project.rootDir.absolutePath, "conf", "application.conf")
    val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")
    val ebeanConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "ebean.yml")
    this.jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dlogback.configurationFile=${logbackConfPath}",
        "-Dprops.file=${ebeanConfPath}",
        "-Dconfig.file=${appconf}"
    )
}

application {
    mainClass.set("myquant.MainApp")
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

runtime {
    this.options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
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
        java.srcDirs.add(file("${buildDir.path}/generated/source/kapt/main"))
    }
}

val distZip: Zip by tasks
distZip.enabled = true
distZip.into("${project.name}-${project.version}") {
    from(".").include("conf/**")
}

val distTar: Tar by tasks
distTar.enabled = false
distTar.into("${project.name}-${project.version}") {
    from(".").include("conf/**")
}

val shadowDistZip :Zip by tasks
shadowDistZip.enabled = false
shadowDistZip.into("${project.name}-shadow-${project.version}") {
    from(".").include("conf/**")
}

val shadowDistTar : Tar by tasks
shadowDistTar.enabled = false
shadowDistTar.into("${project.name}-shadow-${project.version}") {
    from(".").include("conf/**")
}
