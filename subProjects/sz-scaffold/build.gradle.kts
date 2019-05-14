import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    api(project(":subProjects:sz-tools"))

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api("io.vertx:vertx-core:3.7.0")
    api("io.vertx:vertx-web:3.7.0")
    api("io.vertx:vertx-codegen:3.7.0")
    api("io.vertx:vertx-ignite:3.7.0")
    api("io.vertx:vertx-zookeeper:3.7.0")
    api("io.vertx:vertx-lang-kotlin:3.7.0") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("io.vertx:vertx-lang-kotlin-coroutines:3.7.0") {
        this.exclude(group = "org.jetbrains.kotlin")
    }

    api("com.google.guava:guava:27.0-jre")
    api("org.freemarker:freemarker:2.3.23")
    api("redis.clients:jedis:2.9.0")
    api("com.github.ben-manes.caffeine:caffeine:2.6.2")

    configurations.all {
        this.exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
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

