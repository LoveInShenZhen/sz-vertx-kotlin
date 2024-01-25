plugins {
    kotlin("jvm").version("1.9.22")
    id("io.ebean").version("13.6.4")
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

    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ebean:ebean:13.6.4")
    implementation("com.github.ajalt.clikt:clikt:3.1.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("com.squareup:kotlinpoet:1.12.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
//    implementation("com.google.guava:guava:28.2-jre")
    implementation("org.jodd:jodd-util:6.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")


    testImplementation("junit", "junit", "4.12")
}

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
}

application {
    mainClass.set("sz.ebean.gen.AppKt")
}