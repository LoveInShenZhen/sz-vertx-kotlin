import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("io.ebean").version("13.25.0")
    kotlin("kapt")
}

dependencies {
    api(project(":subProjects:sz-ebean"))

//    kapt("io.ebean:kotlin-querybean-generator:13.25.0")

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

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
}
