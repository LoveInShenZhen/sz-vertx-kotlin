/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn how to create Gradle builds at https://guides.gradle.org/creating-new-gradle-builds/
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version("1.9.24").apply(false)
}

allprojects {
    group = "com.github.kklongming"
    version = "4.0.0-dev"

    System.getProperty("version")?.apply {
        version = this
    }

    repositories {
        mavenLocal()
//        maven(url="https://maven.aliyun.com/repository/public/")
        mavenCentral()
    }

}