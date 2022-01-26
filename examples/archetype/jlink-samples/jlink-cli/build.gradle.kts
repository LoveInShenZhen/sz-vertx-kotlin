plugins {
    kotlin("jvm") version "1.6.10"
    java
    application
    id("org.beryx.runtime") version "1.12.7"
}

group = "com.github.kklongming"
version = "1.0.0"

repositories {
    maven {
        url= uri("https://maven.aliyun.com/repository/public/")
    }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
//    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.ajalt.clikt:clikt:3.3.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("ch.qos.logback:logback-classic:1.2.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("sz.cli.CmdApp")
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"

}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"

}