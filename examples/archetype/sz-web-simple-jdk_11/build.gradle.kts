import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version("1.5.31")

    // Apply the application plugin to add support for building a CLI application.
    application

    id("io.ebean").version("12.6.2")
    kotlin("kapt").version("1.5.31")
}


repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenLocal()
    mavenCentral()
    maven(url="https://maven.aliyun.com/repository/public/")
}

dependencies {
    implementation(kotlin("reflect"))
    
    api(files("conf"))
    api("com.github.kklongming:sz-scaffold:4.0.0-dev")
    api("com.github.kklongming:sz-ebean:4.0.0-dev")
    api("com.github.kklongming:sz-api-doc:4.0.0-dev")
    api("com.h2database:h2:1.4.200")


    kapt("io.ebean:kotlin-querybean-generator:12.1.8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    configurations.all {
        this.exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
}

application {
    // Define the main class for the application.
    mainClass.set("com.api.server.ApiServer")
    // 可以在此添加jvm内存参数, eg: '-Xms512m', '-Xmx4096m'
    applicationDefaultJvmArgs = listOf("-Duser.timezone=GMT+8", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

val distZip: Zip by tasks
distZip.into(project.name) {
    from(".")
    include("conf/**")
    include("webroot/**")
}

val distTar: Tar by tasks
distTar.enabled = false

val installDist: Sync by tasks
installDist.into("conf") {
    from("./conf")
    include("**")
}
installDist.into("webroot") {
    from("./webroot")
    include("**")
}

ebean {
    debugLevel = 2
    queryBeans = false
    kotlin = true
    //generatorVersion = "11.4"
}

tasks.register<Delete>("removeLocalSzJarsCache") {
    val localMaven = this.project.repositories.mavenLocal()
    val path = localMaven.url.path + listOf("com", "github", "kklongming").joinToString(separator = File.separator)
    this.delete(path)
    println("remove Local Maven Cache For Sz Framework: $path")
    val gradleJsrCachePath = listOf(this.project.gradle.gradleUserHomeDir.path, "caches", "modules-2", "files-2.1", "com.github.kklongming").joinToString(separator = File.separator)
    val gradleMetaCachePath1 = listOf(this.project.gradle.gradleUserHomeDir.path, "caches", "modules-2", "metadata-2.23", "descriptors", "com.github.kklongming").joinToString(separator = File.separator)
    val gradleMetaCachePath2 = listOf(this.project.gradle.gradleUserHomeDir.path, "caches", "modules-2", "metadata-2.71", "descriptors", "com.github.kklongming").joinToString(separator = File.separator)
    this.delete(gradleJsrCachePath, gradleMetaCachePath1, gradleMetaCachePath2)
    println("remove Local Gradle Cache For Sz Framework: $gradleJsrCachePath")
}

//sourceSets {
//    main {
//        java.srcDirs.add(file("${buildDir.path}/generated/source/kaptKotlin/main"))
//    }
//}
