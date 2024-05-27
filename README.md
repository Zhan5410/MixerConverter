# MixerConverter

一個小小的自用腳本，也是課堂上的期末報告  

## 目錄

1. [簡介](#簡介)
2. [架構](#架構)
3. [功能實現](#施工中)

## 簡介

Mixer 做為一個 Android 平台的免費音樂播放軟體，擁有許多用戶，不過在Youtube 政策更新之後，不再能簡單的轉換並下載歌曲了，而 Mixer 本身也不具有批量下載歌曲的功能，無法透過下載來離線播放，或是透過 USB 等方式，在不支援串流平台播放功能的車上，播放自己的歌單，這隻程式是為了完成這個目的而開發的腳本

## 架構

嘗試使用MVVM模式開發，即 Model - View - ViewModel，同時嘗試使用toml來集中管理依賴版本，以下將分開介紹

### setting

包含關於一些我覺得需要提及的專案設定，諸如版本及權限等等

```xml
<!-- 開啟網路權限 -->
<uses-permission android:name="android.permission.INTERNET"/>
```

### Version_Controller(lib.versions.toml & build.gradle.kts)

將所有依賴庫變數集中在 [lib.versions.toml](./gradle/libs.versions.toml)，並在 [build.gradle.kts](./app/build.gradle.kts)中引用，從外部引入的庫是 : jsoup、gson、coil 三個，其他請見檔案內容

```toml
<toml>
jsoup = "1.17.2"
gson = "2.11.0"
coil = "2.4.0"

.
.
.

jsoup = {group = "org.jsoup", name = "jsoup", version.ref = "jsoup"}
coil = {group = "io.coil-kt", name = "coil", version.ref = "coil"}
coil-compose = {group = "io.coil-kt", name = "coil-compose", version.ref = "coil"}
gson = {group = "com.google.code.gson", name = "gson", version.ref = "gson"}
```

然後引用

```kts
<build.gradle>
dependencies {

    implementation(libs.jsoup) // HTML解析庫
    implementation(libs.gson) // JSON解析庫
    implementation(libs.coil) // 圖片加載庫
    implementation(libs.coil.compose)
    .
    .
    .
}
```

### MainActivity.kt(View)

### ViewModel.kt(ViewModel)

### DataBase.kt(Model)

## 施工中🚧

- [x] 從Mixer匯入歌單
- [ ] 透過Mixer新增歌曲
- [ ] 刪除某個歌單
- [x] 刪除歌單裡特定歌曲
- [ ] 查詢歌曲
- [ ] 匯出成檔案或建立YT播放清單
