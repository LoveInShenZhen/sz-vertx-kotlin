include("subProjects:jodd-dependency")
include("subProjects:sz-crypto")
include("subProjects:sz-log")
include("subProjects:sz-tools")
include("subProjects:sz-scaffold")
include("subProjects:sz-ebean")
include("subProjects:sz-api-doc")
include("subProjects:sz-plan-task")

pluginManagement {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
}