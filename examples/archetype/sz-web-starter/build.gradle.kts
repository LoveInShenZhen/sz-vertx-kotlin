import java.nio.file.Path

plugins {
  kotlin("jvm") version "1.9.24"
  application
}

group = "com.quantplus"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenLocal()
  maven {
    url = uri("https://maven.aliyun.com/repository/public/")
  }
  mavenCentral()
}


dependencies {
  implementation("com.github.kklongming:sz-scaffold:4.0.0-dev")
  implementation("com.github.kklongming:sz-api-doc:4.0.0-dev")

//  implementation("com.github.ajalt.clikt:clikt:4.2.2")

  testImplementation(kotlin("test"))
}

kotlin { // Extension for easy setup
  jvmToolchain(21) // Target version of generated JVM bytecode. See 7️⃣
}

val jvm_args_for_encoding = listOf(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8",
  "-Dsun.jnu.encoding=UTF-8"
)

application {
  mainClass.set("com.quantplus.MainApp")
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
  systemProperty("file.encoding", "UTF-8")
  val appconf = Path.of(this.project.rootDir.absolutePath, "conf", "application.conf")
  val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")

  this.jvmArgs(
    "-Dlogback.configurationFile=${logbackConfPath}",
    "-Dconfig.file=${appconf}",
    "-Dsz.app.home=${this.project.rootDir.absolutePath}"
  )

  // 为了解决 gradle run 时, 终端输出中文乱码的问题
  this.jvmArguments.addAll(jvm_args_for_encoding)
}

tasks.withType<CreateStartScripts> {

  this.doLast {
    val unix_script_lines = unixScript.readLines()
    val new_unix_script_lines = StringBuilder()
    unix_script_lines.forEach { line ->
      if (line.startsWith("DEFAULT_JVM_OPTS=")) {
        new_unix_script_lines.appendLine("DEFAULT_JVM_OPTS=\"-Dlogback.configurationFile=\$APP_HOME/conf/logback.xml -Dsz.app.home=\$APP_HOME -Dconfig.file=\$APP_HOME/conf/application.conf\"")
      } else {
        new_unix_script_lines.appendLine(line)
      }
    }

    unixScript.writeText(new_unix_script_lines.toString())

    val win_script_lines = windowsScript.readLines()
    val new_win_script_lines = StringBuilder()
    win_script_lines.forEach { line ->
      if (line.startsWith("set DEFAULT_JVM_OPTS=")) {
        new_win_script_lines.appendLine("set DEFAULT_JVM_OPTS=-Dlogback.configurationFile=%APP_HOME%\\conf\\logback.xml -Dsz.app.home=\$APP_HOME -Dconfig.file=%APP_HOME%\\conf\\application.conf")
      } else {
        new_win_script_lines.appendLine(line)
      }
    }
    windowsScript.writeText(new_win_script_lines.toString())
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
