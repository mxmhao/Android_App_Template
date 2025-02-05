plugins {
    alias(libs.plugins.android.library)
    // 插件必须要
    `maven-publish`
}

android {
    namespace = "min.test.publishingtemp"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        // 这个必须配置，否则 components["release"] 会报错
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    // 引入本地库
//    implementation("$localGroup:$localArtifactId:$versionName")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val localGroup = "test.mxm.local"
val localArtifactId = "xxxAndroid"
val versionName = "1.0.0"
val localMavenRepoUrl = "../LocalMavenRepo"

/*
  此模块只是演示 maven-publish 发布发到本地的 build.gradle.kts 写法。
  官方详细教程： https://developer.android.google.cn/build/publish-library?hl=zh-cn
  把本地aar依赖库发布到本地 maven 源，以下代码必须放在 module 的 build.gradle 文件中，
  否则 Android Studio 的 gradle 工具中看不到 publishing 任务，
  双击 publishing 任务中的 publish 即可发布到本地，然后就可以看到 app 文件夹的平级文件夹 LocalMavenRepo
  如果需要引入 LocalMavenRepo 源中的库，请在 settings.gradle.kts 的 repositories 中加入 maven ( url = uri("./LocalMavenRepo"))
 */
publishing {
    repositories {
        maven {
            //上传到项目本地仓库
            url = uri(localMavenRepoUrl)
        }
    }

    publications {
        register<MavenPublication>("release") {
            groupId = localGroup
            artifactId = localArtifactId
            version = versionName
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}