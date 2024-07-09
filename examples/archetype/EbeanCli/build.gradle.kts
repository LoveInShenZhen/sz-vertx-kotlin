import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path


plugins {
    kotlin("jvm") version "2.0.0"
    id("org.beryx.runtime") version "1.12.7"
    id("io.ebean").version("15.3.0")
    id("org.jetbrains.kotlin.kapt") version "2.0.0"
    id("com.github.johnrengelman.shadow") version("7.1.2")
    application
}

group = "com.myquant"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
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

    implementation("io.ebean:ebean:15.3.0")
    kapt("io.ebean:querybean-generator:15.3.0")
    implementation("io.ebean:jakarta-persistence-api:3.0")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.+")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.+")
    implementation("org.jodd:jodd-util:6.1.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.14.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
    implementation("org.apache.commons:commons-csv:1.11.0")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
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
