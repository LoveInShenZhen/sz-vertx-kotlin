import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

val vertxVersion = "5.0.5"

dependencies {
    api(project(":subProjects:sz-tools"))
    api(project(":subProjects:sz-crypto"))

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api("io.vertx:vertx-core:$vertxVersion")
    api("io.vertx:vertx-web:$vertxVersion")
    api("io.vertx:vertx-zookeeper:$vertxVersion") {
        this.exclude(group="log4j")
    }
    api("io.vertx:vertx-lang-kotlin:$vertxVersion") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion") {
        this.exclude(group = "org.jetbrains.kotlin")
    }

    // 参考: https://vertx.io/docs/vertx-core/kotlin/#_native_transports
    // 注意, 要保持版本号和vertx依赖的 netty 的版本号一致
    // 建议在自己工程的gradle添加对应的依赖, 作为 runtime 依赖进行添加
//    api(group = "io.netty", name = "netty-transport-native-epoll", version = "4.1.74.Final", classifier = "linux-x86_64")
//    api(group = "io.netty", name = "netty-transport-native-kqueue", version = "4.1.74.Final", classifier = "osx-x86_64")

    api("org.apache.commons:commons-pool2:2.12.1")
    api("com.google.guava:guava:33.5.0-jre")
    api("org.freemarker:freemarker:2.3.34")
//    api("org.kodein.di:kodein-di-generic-jvm:6.5.1")

    testImplementation(kotlin("test"))

    configurations.all {
        this.exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
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

kotlin { // Extension for easy setup
    jvmToolchain(17) // Target version of generated JVM bytecode. See 7️⃣
}

