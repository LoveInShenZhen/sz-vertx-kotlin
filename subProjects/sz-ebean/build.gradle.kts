import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    id("maven-publish")
    id("io.ebean").version("12.6.2")
    kotlin("kapt")
}

dependencies {
    api(project(":subProjects:sz-scaffold"))
    api(project(":subProjects:sz-crypto"))

    api("io.ebean:ebean:12.6.2")
    api("io.ebean:ebean-querybean:12.6.2")
//    kapt("io.ebean:kotlin-querybean-generator:12.6.2")

    api("com.zaxxer:HikariCP:3.3.1")
    api("mysql:mysql-connector-java:8.0.18")
    api("org.glassfish.jaxb:jaxb-runtime:2.3.2")
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

//ebean {
//    debugLevel = 2
//    queryBeans = true
//    kotlin = true
////    generatorVersion = "11.4"
//}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
    
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
    
}