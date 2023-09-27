plugins {
    id("org.jetbrains.kotlin.js") version "1.9.10"
}

group = "org.sz.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
}

kotlin {
    js {
        nodejs {
        }
        binaries.executable()
    }
}