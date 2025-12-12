include("subProjects:jodd-dependency")
include("subProjects:sz-crypto")
include("subProjects:sz-log")
include("subProjects:sz-tools")
include("subProjects:sz-scaffold")
include("subProjects:sz-api-doc")

pluginManagement {
    repositories {
        mavenLocal()
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://plugins.gradle.org/m2/")
        gradlePluginPortal()
    }
}