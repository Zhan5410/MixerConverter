package com.example.mixerconverter

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream

class MyViewModel(application: Application) : AndroidViewModel(application) {
    val refUrl = "https://www.mbplayer.com/list/125368176"
    val apiUrl =
        "https://www.mbplayer.com/api/playlist?reverse=true&type=playlist&vectorId=125368176&firstLaunch=1714635076160"

    val ytUrl = "https://www.youtube.com/watch?v="

    private val _songflow: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val songflow: StateFlow<List<Song>> = _songflow

    var songDao: SongDao? = null

    init {
        viewModelScope.launch {
            songDao = SongDataBase.getInstance(application).SongDao()
            setSong(refUrl, apiUrl)
        }
    }

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

                        val url = ytUrl + id

                        //回傳並add到List裡
                        val songData: Song = Song(songId = id, songImg = img, songName = name, songURL = url)
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

    suspend fun deleteSong(song: Song){
        withContext(Dispatchers.IO){
            try {
                Log.d("delete", "deleteFun be called")
                songDao?.delete(song)
                updateSong(song)
            } catch (e: Exception){
                Log.d("delete error", "delete error : " + e.message.toString())
            }
        }
    }

    private fun updateSong(song: Song){
        _songflow.value = _songflow.value.filter { it.songId != song.songId }
    }

    suspend fun exportSongURLtoFile(context: Context, filename: String){
        withContext(Dispatchers.IO){
            val songurls = songDao?.getSongURL()
            val file = File(context.getExternalFilesDir(null), filename)
            FileOutputStream(file).use {fos ->
                songurls?.forEach {url ->
                    fos.write((url + System.lineSeparator()).toByteArray())
                }
            }
        }
    }

}
