pluginManagement {
    repositories {
        maven ( url = "https://maven.aliyun.com/repository/gradle-plugin/")
        // 有些源没有及时同步国外的源，导致有些库文件还是会从国外源下载，所以多配几个国内源，降低这种概率，说不定总有一个源及时同步了
        maven ( url = "https://maven.aliyun.com/repository/public/")
        maven ( url = "https://maven.aliyun.com/repository/google/")
        maven ( url = "https://repo.huaweicloud.com/repository/maven/")
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
        // 有些源没有及时同步国外的源，导致有些库文件还是会从国外源下载，所以多配几个国内源，降低这种概率，说不定总有一个源及时同步了
        // 建议用 Android Studio 内的按钮去同步下载第三方库，不要用 gradlew build 等命令去同步下载第三方库，
        // 因为奇怪的JDK环境可能导致一些奇奇怪怪的问题导致同步失败。第一次同步最好打开 Android Studio 的 build 视图，
        // 查看 Download info，及时发现哪些库会最终使用国外源，可及时打开代理或者VPN等
        maven ( url = "https://maven.aliyun.com/repository/public/")
        maven ( url = "https://maven.aliyun.com/repository/google/")
        maven ( url = "https://repo.huaweicloud.com/repository/maven/")
        maven ( url = "https://jitpack.io")
        maven ( url = uri("./LocalMavenRepo"))
        google()
        mavenCentral()
        // 本地 libs
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "Android_App_Template"
include(":app")
include(":publishingTemp")
