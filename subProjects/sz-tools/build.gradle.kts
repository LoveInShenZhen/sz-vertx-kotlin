import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
//    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api(project(":subProjects:jodd-dependency"))
    api(project(":subProjects:sz-log"))

    api("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.12.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.12.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.12.2")

    api("com.typesafe:config:1.4.1")
    api("org.apache.commons:commons-lang3:3.9")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
    
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
    
}
