plugins {
    kotlin("jvm") version "1.9.24"
    java
    application
}

group = "com.github.kklongming"
version = "1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.2.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("io.github.config4k:config4k:0.4.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.jodd:jodd-core:5.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("sz.cli.CmdApp")
}

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
}

val run: JavaExec by tasks
run.setJvmArgs(listOf("-Dconfig.file=${project.file("src/dist/conf/application.conf").absolutePath}"))