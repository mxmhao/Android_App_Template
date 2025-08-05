// https://developer.android.google.cn/studio/projects/android-library
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// 上面要 import
val keystoreProperties = Properties()
// 自定义keystore本地配置。放在项目根目录下
val file = rootProject.file("keystore.properties");
if (file.exists()) {
    file.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "min.test.android_app_template"
    compileSdk = 35

    defaultConfig {
        applicationId = "min.test.android_app_template"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // 自定义打包名称，不完全自定义，后面自动会加上${buildType.name}
        base.archivesName = "Template-v$versionName-$versionCode-" + (Math.floor(Math.random() * 1000000) + 1).toInt()
        // 替换值
        manifestPlaceholders += mapOf(
            "AAA" to "1111",
        )
        ndk.abiFilters += mutableSetOf("arm64-v8a", "armeabi-v7a", "x86_64")
        /*splits {
            // 根据像素密度生成多个单一apk
            density {
                isEnable = true
                reset()
                include("hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
            }
            // 根据ABI生成多个单一apk
            abi {
                // Enables building multiple APKs per ABI.
                isEnable = true

                // By default all ABIs are included, so use reset() and include to specify that we only
                // want APKs for x86 and x86_64.

                // Resets the list of ABIs that Gradle should create APKs for to none.
                reset()

                // 不能与 ndk.abiFilters 同时配置，否则报错
                // Specifies a list of ABIs that Gradle should create APKs for.
                include("arm64-v8a", "armeabi-v7a")//"x86", "x86_64"

                // Specifies that we do not want to also generate a universal APK that includes all ABIs.
                isUniversalApk = false
            }
        }*/

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // 必须放在 buildTypes 上面
    signingConfigs {
        // release 不存在要自己创建
        create("release") {
//            keyAlias = keystoreProperties["keyAlias"] as String
//            keyPassword = keystoreProperties["keyPassword"] as String
//            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
//            storePassword = keystoreProperties["storePassword"] as String
        }
        // debug 本身就有
        getByName("debug") {
//            keyAlias = keystoreProperties["keyAlias"] as String
//            keyPassword = keystoreProperties["keyPassword"] as String
//            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
//            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
//        compose = true
        // 支持视图绑定
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
//    implementation(libs.androidx.activity)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.okhttp)
    // 跟踪和报告应用的各种运行时指标
    implementation(libs.androidx.metrics.performance)
    // 读取和写入图片文件 EXIF 标记
    implementation(libs.androidx.exifinterface)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    // fileTree 写法
    implementation(fileTree("libs") {
        include("*.aar", "*.jar")
    })

    // 在 Android Studio 中准确衡量代码性能, 性能测试框架
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    // 远程测试框架
    androidTestImplementation(libs.androidx.uiautomator)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}