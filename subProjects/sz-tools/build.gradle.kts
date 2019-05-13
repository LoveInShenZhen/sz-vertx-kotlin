import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0")

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    api(project(":subProjects:jodd-dependency"))

    api("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.9.8")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.9.8")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.8")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.9.8")

    api("com.typesafe:config:1.3.1")
    api("ch.qos.logback:logback-classic:1.2.3")
    api("org.apache.commons:commons-lang3:3.9")
    api("org.bouncycastle:bcprov-jdk15on:1.58")
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
