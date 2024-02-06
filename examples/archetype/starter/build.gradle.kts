import java.nio.file.Path

plugins {
  kotlin("jvm") version "1.9.22"
  application
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  maven {
    url = uri("https://maven.aliyun.com/repository/public/")
  }
  mavenLocal()
  mavenCentral()
}

val vertxVersion = "4.5.2"
val junitJupiterVersion = "5.9.1"


dependencies {
  implementation(kotlin("stdlib-jdk8"))

  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-lang-kotlin")

  implementation("ch.qos.logback:logback-classic:1.2.11")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
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
  this.applicationDefaultJvmArgs = jvm_args_for_encoding
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
  systemProperty("file.encoding", "UTF-8")
  val appconf = Path.of(this.project.rootDir.absolutePath, "conf", "application.conf")
  val logbackConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "logback.xml")
//  val ebeanConfPath = Path.of(this.project.rootDir.absolutePath, "conf", "ebean.yml")

  this.jvmArgs(
    "-Dlogback.configurationFile=${logbackConfPath}",
    "-Dconfig.file=${appconf}"
  )

  this.jvmArguments.addAll(jvm_args_for_encoding)

  println(this.jvmArgs?.joinToString(" "))
}

tasks.withType<CreateStartScripts> {

  this.doLast {
    val unix_script_lines = unixScript.readLines()
    val new_unix_script_lines = StringBuilder()
    unix_script_lines.forEach { line ->
      if (line.startsWith("DEFAULT_JVM_OPTS=")) {
        new_unix_script_lines.appendLine("DEFAULT_JVM_OPTS=\"-Dlogback.configurationFile=\$APP_HOME/conf/logback.xml -Dconfig.file=\$APP_HOME/conf/application.conf\"")
      } else {
        new_unix_script_lines.appendLine(line)
      }
    }

    unixScript.writeText(new_unix_script_lines.toString())

    val win_script_lines = windowsScript.readLines()
    val new_win_script_lines = StringBuilder()
    win_script_lines.forEach { line ->
      if (line.startsWith("set DEFAULT_JVM_OPTS=")) {
        new_win_script_lines.appendLine("set DEFAULT_JVM_OPTS=-Dlogback.configurationFile=%APP_HOME%\\conf\\logback.xml -Dconfig.file=%APP_HOME%\\conf\\application.conf")
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
