/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn how to create Gradle builds at https://guides.gradle.org/creating-new-gradle-builds/
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("org.jetbrains.kotlin.jvm").version("1.3.71").apply(false)
}

allprojects {
    group = "com.github.kklongming"
    version = "3.1.0-dev"

    System.getProperty("version")?.apply {
        version = this
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

}