plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "myquant.cn.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/io.grpc/grpc-netty
    implementation("io.grpc:grpc-netty:1.54.2")
    implementation("myquant.cn:myquant-kroto:1.0-SNAPSHOT")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.14.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.3") {
        this.exclude(group = "org.jetbrains.kotlin")
    }
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.3")

    implementation("org.jetbrains.kotlinx:dataframe:0.13.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("MainKt")
}

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
}