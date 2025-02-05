pluginManagement {
    repositories {
        maven ( url = "https://maven.aliyun.com/repository/gradle-plugin/")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven ( url = "https://maven.aliyun.com/repository/public/")
        maven ( url = "https://jitpack.io")
        maven ( url = uri("./LocalMavenRepo"))
        google()
        mavenCentral()
    }
}

rootProject.name = "Android_App_Template"
include(":app")
include(":publishingTemp")
