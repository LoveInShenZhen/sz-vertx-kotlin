import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version("1.9.24")

    // Apply the application plugin to add support for building a CLI application.
    application

    id("io.ebean").version("12.13.0")
    kotlin("kapt").version("1.9.24")
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

    api("com.github.kklongming:sz-scaffold:4.0.0-dev")
    api("com.github.kklongming:sz-ebean:4.0.0-dev")
    api("com.github.kklongming:sz-api-doc:4.0.0-dev")

    implementation("com.h2database:h2:1.4.200")
//    runtimeOnly(("mysql:mysql-connector-java:8.0.18"))

    kapt("io.ebean:kotlin-querybean-generator:12.13.0")

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

kotlin { // Extension for easy setup
    jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
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
    queryBeans = true
    kotlin = true
    //generatorVersion = "11.4"
}

val run: JavaExec by tasks
run.jvmArgs = listOf("-Dconfig.file=${project.file("conf/application.conf").absolutePath}")

//sourceSets {
//    main {
//        java.srcDirs.add(file("${buildDir.path}/generated/source/kaptKotlin/main"))
//    }
//}
