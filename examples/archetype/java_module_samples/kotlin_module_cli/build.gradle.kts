/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.7.1/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm").version("1.8.10")
    id("org.beryx.jlink").version("2.25.0")

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenLocal()
    maven(url="https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainModule.set("org.sz.example.modulecli")
    // Define the main class for the application.
    mainClass.set("org.sz.AppKt")
}

//java {
//    modularity.inferModulePath.set(true)
//}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"

}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"

}

jlink {
    this.options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    this.launcher {
        name = "consoleApp"
    }
}