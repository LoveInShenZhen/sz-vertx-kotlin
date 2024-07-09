import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Path


plugins {
    kotlin("jvm") version "2.0.0"
    id("org.beryx.runtime") version "1.12.7"
    id("io.ebean").version("13.10.1")
    id("org.jetbrains.kotlin.kapt") version "2.0.0"
    application
}

group = "com.github.kklongming"
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

    implementation("io.ebean:ebean:13.10.1")
//    implementation("io.ebean:ebean-ddl-generator:13.6.4")
    kapt("io.ebean:querybean-generator:13.10.1")


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
        "-Dlogback.configurationFile=${logbackConfPath}",
        "-Dprops.file=${ebeanConfPath}",
        "-Dconfig.file=${appconf}"
    )
}

application {
    mainClass.set("sz.simple.MainApp")
}

tasks.withType<CreateStartScripts> {

    this.doLast {
        val lines = unixScript.readText().split("\n")
        val newLines = StringBuilder()
        lines.forEach { line ->
            if (line.startsWith("DEFAULT_JVM_OPTS=")) {
                newLines.appendLine("DEFAULT_JVM_OPTS=\"-Dlogback.configurationFile=\$APP_HOME/conf/logback.xml -Dprops.file=\$APP_HOME/conf/ebean.yml -Dconfig.file=\$APP_HOME/conf/application.conf\"")
            } else {
                newLines.appendLine(line)
            }
        }

        unixScript.writeText(newLines.toString())
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
distZip.into("${project.name}-${project.version}") {
    from(".").include("conf/**")
}

val distTar: Tar by tasks
distTar.enabled = false

