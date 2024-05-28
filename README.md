# MixerConverter

一個小小的自用腳本，也是課堂上的期末報告  

## 目錄

- [MixerConverter](#mixerconverter)
  - [目錄](#目錄)
  - [簡介](#簡介)
  - [架構](#架構)
    - [setting](#setting)
    - [Version\_Controller(lib.versions.toml \& build.gradle.kts)](#version_controllerlibversionstoml--buildgradlekts)
    - [MainActivity.kt(View)](#mainactivityktview)
    - [SongItem結構](#songitem結構)
    - [DataBase.kt(Model)](#databasektmodel)
    - [ViewModel.kt(ViewModel)](#viewmodelktviewmodel)
    - [setSong](#setsong)
    - [FetchSongOnMixer](#fetchsongonmixer)
    - [deleteSong](#deletesong)
    - [updateSong](#updatesong)
    - [exportSongtoFile](#exportsongtofile)
  - [施工中🚧](#施工中)

## 簡介

Mixer 做為一個 Android 平台的免費音樂播放軟體，擁有許多用戶，不過在Youtube 政策更新之後，不再能簡單的轉換並下載歌曲了，而 Mixer 本身也不具有批量下載歌曲的功能，無法透過下載來離線播放，或是透過 USB 等方式，在不支援串流平台播放功能的車載音響上，播放自己的歌單，這隻程式是為了完成這個目的而開發的腳本

## 架構

嘗試使用MVVM模式開發，即 Model - View - ViewModel，同時嘗試使用toml來集中管理依賴版本，以下將分開介紹

### setting

包含關於一些我覺得需要提及的專案設定，諸如版本及權限等等

1. 由於用到爬蟲，所以要事先打開網路權限

```xml
<!-- 開啟網路權限 -->
<uses-permission android:name="android.permission.INTERNET"/>
```

2. 最小SDK設定為26，目標SDK和編譯SDK設定為34，運行的模擬器為Google Pixel 6 Pro，SDK版本為31

```kts
    compileSdk = 34 // 設定編譯SDK版本
    minSdk = 26 // 最低支持的SDK版本
    targetSdk = 34 // 目標SDK版本
```

### Version_Controller(lib.versions.toml & build.gradle.kts)

將所有依賴庫變數集中在 [lib.versions.toml](./gradle/libs.versions.toml)，並在 [build.gradle.kts](./app/build.gradle.kts)中引用，從外部引入的庫是 : jsoup、gson、coil 三個，另外使用了room及kpt，其他請見檔案內容

```toml
<toml>
jsoup = "1.17.2"
gson = "2.11.0"
coil = "2.4.0"
room = "2.6.1"

.
.
.

jsoup = {group = "org.jsoup", name = "jsoup", version.ref = "jsoup"}
coil = {group = "io.coil-kt", name = "coil", version.ref = "coil"}
coil-compose = {group = "io.coil-kt", name = "coil-compose", version.ref = "coil"}
gson = {group = "com.google.code.gson", name = "gson", version.ref = "gson"}
androidx-room-runtime = {group = "androidx.room", name = "room-runtime", version.ref = "room"}
androidx-room-ktx = {group = "androidx.room", name = "room-ktx", version.ref = "room"}
androidx-room-compiler = {group = "androidx.room", name = "room-compiler", version.ref = "room"}
```

然後引用

```kts
<build.gradle>

// plugins區塊用來應用Gradle插件，這些插件提供了構建和打包Android應用所需的功能。
plugins {
    alias(libs.plugins.androidApplication) // 應用程序模塊的基本插件
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-android") // 支持Kotlin的Android插件
    id("kotlin-kapt") // Kotlin的Kapt插件，用於注解處理
}
.
.
.
dependencies {

    implementation(libs.jsoup) // HTML解析庫
    implementation(libs.gson) // JSON解析庫
    implementation(libs.coil) // 圖片加載庫
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime) // Room數據庫運行時庫
    implementation(libs.androidx.room.ktx) // Room的KTX擴展
    kapt(libs.androidx.room.compiler) // Room數據庫編譯器
    .
    .
    .
}
```

### MainActivity.kt(View)

頁面包含了一個簡單的播放清單，使用了一個LazyColumn顯示歌曲，顯示的歌曲資料包含縮圖、歌名以及一個刪除的按鈕

```kotlin

if (viewModel != null){
        //定義itemState來收集songflow發射的數據並賦值給items
        val itemState = viewModel.songflow.collectAsState()
        items = itemState.value
    } else{
        //使用幾筆假資料以方便預覽
        items = getSample()
    }

if (items.isEmpty()){
    // 加载中显示
    Text(
        text = "載入中...",
        modifier = Modifier
                    .weight(0.8f)
                    .align(Alignment.CenterHorizontally)
        )
    }
else {
    //使用LazyColumn並在裡面呼叫SongItem元件，將items傳入給他
    LazyColumn(
        modifier = Modifier.weight(0.8f),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            items(items) { song ->
                SongItem(items = song, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
```

### SongItem結構

在 IconButton onClick，創建了一個協程調用了 DAO 接口裡的 delete 方法 (按下紅色垃圾桶該首歌將被刪除)

```kotlin
//一個Song元件，傳入Song Type Data
@Composable
private fun SongItem(items: Song, viewModel: MyViewModel?) {
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = Color.Blue.copy(alpha = 0.2f),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //縮圖
            AsyncImage(
                model = items.songImg,
                contentDescription = "",
                modifier = Modifier.size(48.dp)
            )
            //歌名
            Text(
                text = items.songName,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(start = 8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            //Icon按鈕，功能是刪除所以Icon使用垃圾桶並設置紅色
            IconButton(
                onClick = {
                    coroutineScope.launch{
                        viewModel?.deleteSong(items)
                    }
                },
                enabled = true,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "",
                    tint = Color.Red
                )
            }
        }
    }
}
```

### DataBase.kt(Model)

考慮到每首歌在 YouTube 上都有唯一 ID，主鍵沒有使用自動生成而是直接使用歌曲的唯一 ID

1. 歌曲 ID
2. 歌曲在 YouTube 上的 URL (固定格式 + ID)
3. 歌曲的縮圖 URL (固定格式 + ID)
4. 歌名

```kotlin
//定義資料表
@Entity(tableName = "MyPlayList")
data class Song(
    @PrimaryKey @ColumnInfo(name = "Song_id") val songId: String,
    @ColumnInfo(name = "Song_url") val songURL: String,
    @ColumnInfo(name = "Song_Img") val songImg: String,
    @ColumnInfo(name = "Song_name") val songName: String,
)
```

定義了幾個DAO接口，包括 :  

1. 查看整個歌單
2. 獲取歌曲 URL
3. 插入單首歌，插入策略選擇覆蓋
4. 插入複數歌曲，插入策略選擇覆蓋 (本專案在創建歌單時使用)
5. 刪除單首歌

```kotlin
//DAO接口
@Dao
interface SongDao{
    @Query("SELECT * FROM MyPlayList")
    fun getAll(): Flow<List<Song>>

    @Query("SELECT Song_url FROM MyPlayList")
    fun getSongURL(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Delete
    suspend fun delete(song: Song)
}
```

構造子中把匯出打開，定義了 getInstance 方法，使用單例模式確保使用的是同一個實例，同時避免了重複創建。  
另外，雖然 INSTANCE 宣告時可空，回傳時使用了非空斷言

```kotlin
//構造子
@Database(entities = [Song::class], version = 1, exportSchema = true)
abstract class SongDataBase: RoomDatabase(){
    abstract fun SongDao(): SongDao

    companion object{
        private var INSTANCE: SongDataBase? = null

        fun getInstance(context: Context): SongDataBase{
            if (INSTANCE == null){
                synchronized(SongDataBase::class){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        SongDataBase::class.java,
                        "SongDataBase"
                    ).build()
                }
            }
            return INSTANCE!!
        }

        fun destoryInstace(){
            INSTANCE = null
        }

    }
}
```

### ViewModel.kt(ViewModel)

創建幾個變數，其中 :  

1. API來獲取歌單的資料
2. 可變的FLOW更改，不可變的用來讓 view 觀察
3. DAO宣告，並在後續初始化
4. 使用init關鍵字初始化DAO接口，不過依舊創建一個協程，這是為了後續的setSong函數

```kotlin

val refer: String = "Mixer播放清單網址"
val apiURL: String = "API接口"

private val _songflow: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
val songflow: StateFlow<List<Song>> = _songflow

var songDao: SongDao? = null

init {
    viewModelScope.launch {
        songDao = SongDataBase.getInstance(application).SongDao()
        setSong(refUrl, apiUrl)
    }
}
```

### setSong

使用 fetchSongOnMixer 函數，爬蟲獲取資料並插入到資料庫

1. 因為有網路需求(爬蟲)，將整體轉移到 IO 線程中
2. 考慮性能，使用 insertAll 接口，即爬取成一個 List\<Song> 並插入而非一首一首插入

```kotlin
private suspend fun setSong(reference: String, api: String){
    withContext(Dispatchers.IO) {
        try {
            _songflow.value = fetchSongOnMixer(reference, api)
            songDao?.insertAll(_songflow.value)
        }
        catch (e : Exception){
            _songflow.value = emptyList()
            Log.d("set error", e.message.toString())
        }
    }
}
```

### FetchSongOnMixer

1. 切換到 IO 線程
2. 透過 jsoup 爬取內容
3. gson 解析成可操作的形式，篩選出我們需要的歌曲唯一 ID 以及歌名，縮圖 URL 為固定格式加上歌曲 ID
4. 此處考慮性能，回傳的是 List\<Song>，爬取完畢後一次處理

```kotlin
private suspend fun fetchSongOnMixer(reference: String, api: String): MutableList<Song> {
    //一個song的List
    val songDataList = mutableListOf<Song>()

    //切換到IO線程
    withContext(Dispatchers.IO) {
        //爬蟲並轉換成JsonObject
        val document = Jsoup.connect(api)
            .ignoreContentType(true)
            .referrer(reference)
            .get()
        val jsonStr = document.body().text()
        val jsonObject = JsonParser
            .parseString(jsonStr)
            .asJsonObject

        //將JsonObject轉成JsonArray，方便待會遍歷
        val itemListArray = jsonObject.getAsJsonArray("items")

        //遍歷，透過run簡化，"f" 跟 "tt"都有的狀況下做處理，否則統一回傳不存在
        for (element in itemListArray) {
            element.asJsonObject.run {
                if (has("f") && has("tt")) {
                    //"f" 為歌曲的唯一ID
                    val id = element.asJsonObject.get("f").asString

                    //透過ID轉換成可訪問的縮圖
                    val imgUrlStart = "https://i.ytimg.com/vi/"
                    val imgUrlEnd = "/default.jpg"
                    val img = imgUrlStart + id + imgUrlEnd

                    //"tt" 為歌名
                    val name = element.asJsonObject.get("tt").asString

                    //回傳並add到List裡
                    val songData: Song = Song(songId = id, songImg = img, songName = name)
                    songDataList.add(songData)
                } else {
                    println("歌曲不存在")
                }
            }         
       }
    }
        //從IO線程返回後回傳songList
        return songDataList
}
```

### deleteSong

一樣將涉及資料庫的操作切換到 IO 線程，調用 delete 接口並做 update

```kotlin
suspend fun deleteSong(song: Song){        
    withContext(Dispatchers.IO){
        try {
            songDao?.delete(song)
            updateSong(song)
        } catch (e: Exception){
            Log.d("delete error", "delete error : " + e.message.toString())
        }
    }
}
```

### updateSong

對 FLOW 的值做篩選，把被刪除那首歌的 ID 移除 (不等於刪除歌曲 ID 的其他所有歌曲)，如此一來實現了 FLOW 的更改並讓 UI 實時響應變化

```kotlin
private fun updateSong(song: Song){
    _songflow.value = _songflow.value.filter { it.songId != song.songId }
}
```

### exportSongtoFile

目的是將最後獲取的結果匯出成一個 .txt 檔

```kotlin
suspend fun exportSongURLtoFile(context: Context, filename: String){
    withContext(Dispatchers.IO){

        //透過 DAO 接口獲取 urls
        val songurls = songDao?.getSongURL()

        // 獲取外部文件目錄並創建文件
        val file = File(context.getExternalFilesDir(null), filename)

        // 使用FileOutputStream寫入文件
        FileOutputStream(file).use {fos ->

            //遍歷songurls
            songurls?.forEach {url ->

                // 將每個URL與行分隔符拼接後轉換為ByteArray並寫入文件
                fos.write((url + System.lineSeparator()).toByteArray())
            }
        }
    }
}
```

## 施工中🚧

- [x] 從Mixer匯入歌單
- [ ] 透過Mixer新增歌曲
- [ ] 刪除某個歌單
- [x] 刪除歌單裡特定歌曲
- [ ] 查詢歌曲
- [x] 匯出成檔案 ( .txt )
- [ ] 建立YT播放清單
