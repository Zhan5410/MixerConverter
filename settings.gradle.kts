// pluginManagement區塊用來配置如何解析和管理插件。
pluginManagement {
    repositories {
        // google倉庫用來包含與Android相關的組件和插件。
        /*google {
            content {
                // 包含符合正則表達式的組件
                includeGroupByRegex("com\\.android.*") // 包含所有以"com.android"開頭的組件
                includeGroupByRegex("com\\.google.*") // 包含所有以"com.google"開頭的組件
                includeGroupByRegex("androidx.*") // 包含所有以"androidx"開頭的組件
            }
        }*/
        google()
        // mavenCentral是Java和Kotlin等社區廣泛使用的倉庫。
        mavenCentral()
        // gradlePluginPortal是專門用來解析Gradle插件的倉庫。
        gradlePluginPortal()
    }
}

// dependencyResolutionManagement區塊用來配置依賴解析策略。
// 這裡定義了全局倉庫設定。
dependencyResolutionManagement {
    // 設定倉庫模式為RepositoriesMode.FAIL_ON_PROJECT_REPOS。
    // 這意味著如果在子項目中定義了倉庫，則構建將失敗。
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 使用google倉庫。
        google()
        // 使用mavenCentral倉庫。
        mavenCentral()
    }
}

// 設定根項目的名稱。
rootProject.name = "MixerConverter"

// 包含名為":app"的子項目。
include(":app")
