import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("io.ebean").version("12.13.0")
    kotlin("kapt")
}

dependencies {
    api(project(":subProjects:sz-ebean"))

//    kapt("io.ebean:kotlin-querybean-generator:12.13.0")

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

ebean {
    debugLevel = 2
//    queryBeans = false
    kotlin = true
//    generatorVersion = "11.4"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
    
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
    
}
