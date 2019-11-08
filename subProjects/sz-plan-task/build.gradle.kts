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
    }
}

ebean {
    debugLevel = 2
//    queryBeans = false
    kotlin = true
//    generatorVersion = "11.4"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
