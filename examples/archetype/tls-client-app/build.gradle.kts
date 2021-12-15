plugins {
    kotlin("jvm") version "1.6.10"
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
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.ajalt.clikt:clikt:3.3.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("io.github.config4k:config4k:0.4.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("io.github.hakky54:sslcontext-kickstart:7.0.3")
    implementation("io.github.hakky54:sslcontext-kickstart-for-pem:7.0.3")

    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-java:1.6.5")
    implementation("io.ktor:ktor-client-apache:1.6.5")
    implementation("io.ktor:ktor-client-okhttp:1.6.5")
    implementation("io.ktor:ktor-client-logging:1.6.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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

val run: JavaExec by tasks
run.setJvmArgs(listOf("-Dconfig.file=${project.file("src/dist/conf/application.conf").absolutePath}",
"-Djavax.net.debug=ssl"))