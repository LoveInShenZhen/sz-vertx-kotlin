import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api(project(":subProjects:sz-scaffold"))
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
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
    jvmTarget = "11"
    useIR = true
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
    useIR = true
}

