plugins {
    kotlin("jvm") version "1.5.10"
    id("io.ebean").version("12.8.0")
    application
}

group = "sz.utils.ebean"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("io.ebean:ebean:12.8.0")
    implementation("com.github.ajalt.clikt:clikt:3.1.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("mysql:mysql-connector-java:8.0.18")
    implementation("com.squareup:kotlinpoet:1.8.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
//    implementation("com.google.guava:guava:28.2-jre")
    implementation("org.jodd:jodd-util:6.0.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.+")


    testImplementation("junit", "junit", "4.12")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

application {
    mainClass.set("sz.ebean.gen.AppKt")
}