import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
    id("io.ebean").version("12.1.1")
    kotlin("kapt")
}

dependencies {
    api(project(":subProjects:sz-ebean"))

//    kapt("io.ebean:kotlin-querybean-generator:12.1.1")

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

ebean {
    debugLevel = 2
//    queryBeans = false
    kotlin = true
//    generatorVersion = "11.4"
}