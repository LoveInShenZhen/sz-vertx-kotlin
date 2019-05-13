import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("gradle.plugin.io.ebean:ebean-gradle-plugin:11.36.1")
    }
}


plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("io.ebean").version("11.36.1")
    kotlin("kapt")
}

dependencies {
    api(project(":subProjects:sz-scaffold"))

    api("com.zaxxer:HikariCP:3.2.0")
    api("io.ebean:ebean:11.32.1")
    api("io.ebean:ebean-querybean:11.32.1")
    api("mysql:mysql-connector-java:5.1.46")

    kapt("io.ebean:kotlin-querybean-generator:11.4.1")

}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("maven") {

            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

ebean {
    debugLevel = 2
    queryBeans = true
    kotlin = true
    generatorVersion = "11.4"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}