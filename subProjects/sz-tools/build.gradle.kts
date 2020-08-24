import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.4")

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api(project(":subProjects:jodd-dependency"))
    api(project(":subProjects:sz-log"))

    api("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.10.1")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.10.1")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.10.1")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.10.1")

    api("com.typesafe:config:1.3.4")
    api("org.apache.commons:commons-lang3:3.9")
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

    repositories {
        var myRepo = "/Users/kk/ssdwork/github/kklongming.github.io/repository"
        System.getProperty("myRepo")?.apply {
            myRepo = this
        }
        maven {
            name = "myRepo"
            url = uri("file://$myRepo")
        }
        maven {
            name = "wise-repo"
            val releasesRepoUrl = "http://aimid.wise4ai.com:8081/repository/wise-repository/"
            val snapshotsRepoUrl = "http://aimid.wise4ai.com:8081/repository/wise-repository/"
            url = uri(if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl)
            credentials {
                username = "admin"
                password = "admin@wise"
            }
        }
    }
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
