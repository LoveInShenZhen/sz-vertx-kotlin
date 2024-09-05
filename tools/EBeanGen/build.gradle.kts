plugins {
    kotlin("jvm").version("2.0.20")
    id("io.ebean").version("15.3.0")
    application
}

group = "sz.utils.ebean"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.ebean:ebean:15.3.0")
    implementation("io.ebean:jakarta-persistence-api:3.0")
    implementation("com.github.ajalt.clikt:clikt:4.3.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("com.squareup:kotlinpoet:1.15.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
//    implementation("com.google.guava:guava:28.2-jre")
    implementation("org.jodd:jodd-util:6.2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0-rc1")


    testImplementation("junit", "junit", "4.12")
}

kotlin { // Extension for easy setup
    jvmToolchain(17) // Target version of generated JVM bytecode. See 7️⃣
}

application {
    mainClass.set("sz.ebean.gen.AppKt")
}