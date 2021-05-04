import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    api(project(":subProjects:sz-tools"))
    api(project(":subProjects:sz-crypto"))

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api("io.vertx:vertx-core:4.0.3")
    api("io.vertx:vertx-web:4.0.3")
    api("io.vertx:vertx-zookeeper:4.0.3")
    api("io.vertx:vertx-redis-client:4.0.3")
    api("io.vertx:vertx-lang-kotlin:4.0.3") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("io.vertx:vertx-lang-kotlin-coroutines:4.0.3") {
        this.exclude(group = "org.jetbrains.kotlin")
    }

    // 参考: https://vertx.io/docs/vertx-core/kotlin/#_native_transports
    // 注意, 要保持版本号和vertx依赖的 netty 的版本号一致
    api(group = "io.netty", name = "netty-transport-native-epoll", version = "4.1.48.Final", classifier = "linux-x86_64")
    api(group = "io.netty", name = "netty-transport-native-kqueue", version = "4.1.48.Final", classifier = "osx-x86_64")

    api("org.apache.commons:commons-pool2:2.6.2")
    api("com.google.guava:guava:28.2-jre")
    api("org.freemarker:freemarker:2.3.23")
    api("org.kodein.di:kodein-di-generic-jvm:6.5.1")

    configurations.all {
        this.exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
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

