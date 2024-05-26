// plugins區塊用來應用Gradle插件，這些插件提供了構建和打包Android應用所需的功能。
plugins {
    alias(libs.plugins.androidApplication) // 應用程序模塊的基本插件
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-android") // 支持Kotlin的Android插件
    id("kotlin-kapt") // Kotlin的Kapt插件，用於注解處理
}

// android區塊用來配置與Android構建相關的設置。
android {
    namespace = "com.example.mixerconverter"
    compileSdk = 34 // 設定編譯SDK版本

    defaultConfig {
        applicationId = "com.example.mixerconverter" // 應用程序ID
        minSdk = 26 // 最低支持的SDK版本
        targetSdk = 34 // 目標SDK版本
        versionCode = 1 // 應用程序的版本代碼
        versionName = "1.0" // 應用程序的版本名稱

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 指定測試運行器
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // buildTypes區塊定義不同構建類型的設置
    buildTypes {
        release {
            isMinifyEnabled = false // 禁用代碼混淆
            // 指定ProGuard配置文件
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // compileOptions區塊設置Java編譯選項
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // 設置源代碼的兼容性版本
        targetCompatibility = JavaVersion.VERSION_1_8 // 設置目標字節碼的兼容性版本
    }
    // kotlinOptions區塊設置Kotlin編譯選項
    kotlinOptions {
        jvmTarget = "1.8" // 設置JVM目標版本
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// dependencies區塊用來管理應用程序的依賴項。
dependencies {

    implementation(libs.jsoup) // HTML解析庫
    implementation(libs.gson) // JSON解析庫
    implementation(libs.coil) // 圖片加載庫
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime) // Room數據庫運行時庫
    implementation(libs.androidx.room.ktx) // Room的KTX擴展
    kapt(libs.androidx.room.compiler) // Room數據庫編譯器

    implementation(libs.androidx.core.ktx) // 核心KTX庫
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material Design庫
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit) // JUnit測試庫
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit擴展
    androidTestImplementation(libs.androidx.espresso.core) // Espresso測試庫
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// build.gradle

// 定义一个名为 printVariable 的自定义任务
task( "printVariable") {
    // 指定任务的描述
    description = "Prints the value of a specified variable"
    // 指定任务的动作
    doLast {
        // 在任务执行时打印指定变量的值
        println("lib jsoup : ${libs.jsoup}")
        println("lib gson get : ${libs.gson.get()}")
        println("libs coil no get : ${libs.coil}")
        //println("libs coil get : ${libs.coil.get()}")
        println("lib ver coil no get : ${libs.versions.coil}")
        println("lib ver coil : ${libs.versions.coil.get()}")
        // 可以根据需要打印更多变量的值
    }
}