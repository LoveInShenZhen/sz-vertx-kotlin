import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ebean").version("15.5.0")
    id("org.jetbrains.kotlin.kapt") version "2.0.20"
    id("org.beryx.runtime") version "1.12.7"
    application
}

group = "com.kts"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("com.github.ajalt.clikt:clikt:4.3.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("io.github.config4k:config4k:0.7.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:8.0.33")


    implementation("io.ebean:ebean:15.5.0")


    implementation("io.ebean:ebean-ddl-generator:15.5.0")
    kapt("io.ebean:querybean-generator:15.5.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
//    implementation("org.jodd:jodd-util:6.2.2")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
//    implementation("org.apache.commons:commons-lang3:3.14.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
//    implementation("org.apache.commons:commons-csv:1.11.0")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin { // Extension for easy setup
    jvmToolchain(17) // Target version of generated JVM bytecode. See 7️⃣
}

tasks.withType<JavaExec> {
    val appconf = Path.of(this.project.rootDir.absolutePath, "conf", "application.conf")
    val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")
    val ebeanConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "ebean.yml")
    this.jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8",
        "-Dlogback.configurationFile=${logbackConfPath}",
        "-Dprops.file=${ebeanConfPath}",
        "-Dconfig.file=${appconf}"
    )
}

application {
    mainClass.set("kts.MainApp")
}

tasks.withType<CreateStartScripts> {

    this.doLast {
        val unix_script_lines = unixScript.readLines()
        val new_unix_script_lines = StringBuilder()
        unix_script_lines.forEach { line ->
            if (line.startsWith("DEFAULT_JVM_OPTS=")) {
                new_unix_script_lines.appendLine("DEFAULT_JVM_OPTS=\"-Dlogback.configurationFile=\$APP_HOME/conf/logback.xml -Dprops.file=\$APP_HOME/conf/ebean.yml -Dconfig.file=\$APP_HOME/conf/application.conf\"")
            } else {
                new_unix_script_lines.appendLine(line)
            }
        }

        unixScript.writeText(new_unix_script_lines.toString())

        val win_script_lines = windowsScript.readLines()
        val new_win_script_lines = StringBuilder()
        win_script_lines.forEach { line ->
            if (line.startsWith("set DEFAULT_JVM_OPTS=")) {
                new_win_script_lines.appendLine("set DEFAULT_JVM_OPTS=-Dlogback.configurationFile=%APP_HOME%\\conf\\logback.xml -Dprops.file=%APP_HOME%\\conf\\ebean.yml -Dconfig.file=%APP_HOME%\\conf\\application.conf")
            } else {
                new_win_script_lines.appendLine(line)
            }
        }
        windowsScript.writeText(new_win_script_lines.toString())
    }
}

fun currentOsType() : String {
    val os_name = System.getProperty("os.name").lowercase()
    if (os_name.contains("win")) {
        return "win"
    }
    if (os_name.contains("linux")) {
        return "linux"
    }
    if (os_name.contains("mac")) {
        return "mac"
    }

    return "unknown"
}

fun targetOsType(): String {
    val targetOs = System.getProperty("target.os")
    if (targetOs.isNullOrBlank()) {
        return currentOsType()
    } else {
        return targetOs.lowercase()
    }
}

fun jdkHome(): String {
    val os_type = currentOsType()
    val target_os_type = targetOsType()
    if (os_type == target_os_type) {
        val java_home = System.getenv("JAVA_HOME")
        if (java_home.isNullOrBlank()) {
            println("请设置 JAVA_HOME 环境变量")
            throw Exception("请设置 JAVA_HOME 环境变量")
        }
        return java_home
    } else {
        val jdk_home_env_name = "${target_os_type.uppercase()}_JDK_HOME"
        val jdk_home = System.getenv(jdk_home_env_name)
        if (jdk_home.isNullOrBlank()) {
            println("请设置 $jdk_home_env_name 环境变量")
            throw Exception("请设置 $jdk_home_env_name 环境变量")
        }

        return jdk_home
    }
}

runtime {
    this.options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    if (targetOsType() == "win") {
        targetPlatform("win") {
            println("构建 runtime 包目标平台: win")
            setJdkHome(jdkHome())
            addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
        }
    }

    if (targetOsType() == "linux") {
        println("构建 runtime 包目标平台: linux")
        targetPlatform("linux") {
            val linux_jdk_home = System.getenv("LINUX_JDK_HOME")
            if (linux_jdk_home.isNullOrBlank()) {
                println("请设置 LINUX_JDK_HOME 环境变量")
                throw Exception("请设置 LINUX_JDK_HOME 环境变量")
            }
            setJdkHome(linux_jdk_home)
            addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
        }
    }
}


kapt {
    generateStubs = true
}

ebean {
    debugLevel = 1
    queryBeans = true
    kotlin = true
}


sourceSets {
    main {
        java.srcDirs.add(file("${layout.buildDirectory.asFile.get().path}/generated/source/kapt/main"))
    }
}

val distZip: Zip by tasks
distZip.enabled = true
distZip.into("${project.name}-${project.version}") {
    from(".").include("conf/**")
}

val distTar: Tar by tasks
distTar.enabled = false
distTar.into("${project.name}-${project.version}") {
    from(".").include("conf/**")
}

fun execCmd(cmdarray:Array<String>) :String {
    val p = Runtime.getRuntime().exec(cmdarray)
    val resultStream = p.inputStream
    p.waitFor()
    return resultStream.reader().readText().trim()
}

val build by tasks
build.apply {
    doFirst {
        val git_ver_args = arrayOf("git", "rev-parse", "HEAD")
        val git_rev =execCmd(git_ver_args)

        val git_ver_tag = arrayOf("git", "tag | sort -r -V | head -1")
        var ver_tag = execCmd(git_ver_tag)
        if (ver_tag.isBlank()) {
            ver_tag = "v0.0.0"
        }

        val version_file = layout.projectDirectory.file("src/main/resources/version.conf").asFile

        val lines = mutableListOf<String>()
        lines.add("""Version : "${ver_tag}" """)
        lines.add("""GitRev : "${git_rev}" """)
        lines.add("""BuildAt : "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"""")

        version_file.writeText(lines.joinToString("\n"))
        println("构建版本 git rev: ${git_rev}")
    }
}

tasks.register<Copy>("copyConfFiles") {
    dependsOn("runtime")
    val confAssetsSpec: CopySpec = copySpec {
        from(".").include("conf/**")
    }

    into(layout.buildDirectory.dir("image/${project.name}-${targetOsType()}"))
    with(confAssetsSpec)
}

tasks.register<Zip>("distsRuntime") {
    dependsOn("copyConfFiles")

    archiveFileName = "${project.name}-${targetOsType()}.zip"
    destinationDirectory = layout.buildDirectory.dir("runtime_dists")

    from(layout.buildDirectory.dir("image/${project.name}-${targetOsType()}"))
}
