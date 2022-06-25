import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt
import java.nio.file.Path


plugins {
    kotlin("jvm") version "1.7.0"
    application
    id("org.beryx.runtime") version "1.12.7"
    id("io.ebean").version("13.6.4")
    id("org.jetbrains.kotlin.kapt") version "1.7.0"
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
    implementation("io.ebean:ebean:13.6.4")
//    org.jetbrains.kotlin.kapt3.base.Kapt.kapt("io.ebean:querybean-generator:13.6.4")


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
    this.args("--config", appconf.toString())
    val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")
    this.jvmArgs("-Dlogback.configurationFile=$logbackConfPath")
}

application {
    mainClass.set("sz.simple.MainApp")
}

runtime {
    this.options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
}