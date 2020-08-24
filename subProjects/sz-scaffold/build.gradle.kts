import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    id("maven-publish")
}

dependencies {
    api(project(":subProjects:sz-tools"))
    api(project(":subProjects:sz-crypto"))

    // android gradle依赖：implementation 和compile的区别
    // 参考: https://www.jianshu.com/p/f34c179bc9d0 根据需要选择使用不同的依赖设定方式
    api("io.vertx:vertx-core:3.8.5")
    api("io.vertx:vertx-web:3.8.5")
    api("io.vertx:vertx-zookeeper:3.8.5")
    api("io.vertx:vertx-redis-client:3.8.5")
    api("io.vertx:vertx-lang-kotlin:3.8.5") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    api("io.vertx:vertx-lang-kotlin-coroutines:3.8.5") {
        this.exclude(group = "org.jetbrains.kotlin")
    }

    // 参考: https://vertx.io/docs/vertx-core/kotlin/#_native_transports
    // 注意, 要保持版本号和vertx依赖的 netty 的版本号一致
    api(group = "io.netty", name = "netty-transport-native-epoll", version = "4.1.42.Final", classifier = "linux-x86_64")
    api(group = "io.netty", name = "netty-transport-native-kqueue", version = "4.1.42.Final", classifier = "osx-x86_64")

    api("org.apache.commons:commons-pool2:2.6.2")
    api("com.google.guava:guava:28.1-jre")
    api("org.freemarker:freemarker:2.3.23")
    api("com.github.ben-manes.caffeine:caffeine:2.7.0")
//    api("com.github.ben-manes.caffeine:guava:2.7.0")

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

