// plugins 块定义了项目使用的插件
plugins {
    // 应用 Java 插件
    java
    // 应用 application 插件，用于创建可执行的 Java 应用程序
    application
    // 应用 Kotlin JVM 插件
    kotlin("jvm") version "2.1.10"
    // 应用 Java 模块化插件
    id("org.javamodularity.moduleplugin") version "1.8.15"
    // 应用 JavaFX 插件，
    id("org.openjfx.javafxplugin") version "0.1.0"
    // 应用 jlink 插件
    id("org.beryx.jlink") version "3.1.1"
}

// 项目的组名
group = "io.kts"
// 项目的版本号
version = "1.0-SNAPSHOT"

// 配置项目的依赖仓库，使用 Maven 中央仓库
repositories {
    mavenCentral()
}

// 定义额外的属性
val junitVersion = "5.10.2"

// 配置 Java 编译任务的编码为 UTF-8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 配置应用程序的主模块和主类
application {
    mainModule.set("io.kts.javafxdemo")
    mainClass.set("io.kts.javafxdemo.HelloApplication")
}

// 配置 Kotlin 的 JVM 工具链版本为 17
kotlin {
    jvmToolchain(21)
}

// 配置 JavaFX 的版本和使用的模块
javafx {
    version = "17.0.6"
    modules("javafx.controls", "javafx.fxml")
}

// 配置项目的依赖
dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    // 测试实现依赖 JUnit Jupiter API
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    // 测试运行时依赖 JUnit Jupiter 引擎
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

// 配置测试任务使用 JUnit 平台
tasks.test {
    useJUnitPlatform()
}

// 配置 jlink 任务
jlink {
    imageZip.set(project.file("${layout.buildDirectory.asFile.get().path}/distributions/app-${javafx.platform.classifier}.zip"))
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "app"
    }
}