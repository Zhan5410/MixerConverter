package com.example.mixerconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.mixerconverter.ui.theme.MixerConverterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    //創建viewModel來保證全局使用的是同一個
    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MixerConverterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navHostController: NavHostController = rememberNavController()

                    //呼叫MyApp並傳入viewModel
                    MyApp(viewModel, navHostController)
                }
            }
        }

    }
}

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
                        Log.d("click", "IconButton CLick")
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

//定義整個畫面
@Composable
private fun MyApp(viewModel: MyViewModel? = null, navHostController: NavHostController?) {
    val items: List<Song>
    val MockController: NavHostController

    if (viewModel != null){
        //定義itemState來收集songflow發射的數據並賦值給items
        val itemState = viewModel.songflow.collectAsState()
        items = itemState.value
    } else{
        items = getSample()
    }

    if (navHostController == null){
        MockController = rememberNavController()
    } else{
        MockController = navHostController
    }

    AppNavHost(navHostController = MockController, viewModel = viewModel, items = items)
}

private fun getSample(): List<Song>{
    return listOf(
        Song(songId = "1", songName = "这才是《牧马城市》真正的原唱，歌声唱尽辛酸，听得催人泪下", songImg = "https://i.ytimg.com/vi/y_Z2b5HALN4/default.jpg"),
        Song(songId = "2", songName = "这才是《牧马城市》真正的原唱，歌声唱尽辛酸，听得催人泪下", songImg = "https://i.ytimg.com/vi/y_Z2b5HALN4/default.jpg"),
        Song(songId = "3", songName = "这才是《牧马城市》真正的原唱，歌声唱尽辛酸，听得催人泪下", songImg =  "https://i.ytimg.com/vi/y_Z2b5HALN4/default.jpg")
    )
}

private fun ViewPlayList(navController: NavController){

}

@Composable
private fun EditPlayList(navController: NavController, items: List<Song>, viewModel: MyViewModel?){
    Surface(
        modifier = Modifier
            .padding(
                horizontal = 4.dp,
                vertical = 4.dp
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row (
                modifier = Modifier
                    .background(color = Color.Magenta)
                    //.padding(start = 16.dp)
                    //.weight(0.1f)
                    .height(80.dp)
                    .fillMaxWidth()
            ){
                //縮圖
                AsyncImage(
                    model = "",
                    contentDescription = "",
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "this is ontop text",
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.5f)
                )
                //顯示總歌曲數量
                Text(
                    text = "Total Songs : ${items.size}",
                    modifier = Modifier
                        .background(color = Color.LightGray)
                        .padding(end = 16.dp)
                        //.align(Alignment.CenterHorizontally)
                        .weight(0.5f)

                )
            }

            if (items.isEmpty()){
                // 加载中显示
                Text(
                    text = "載入中...",
                    modifier = Modifier
                        //.padding(16.dp)
                        .weight(0.8f)
                        .align(Alignment.CenterHorizontally)
                )
            }
            else {
                //使用LazyColumn並在裡面呼叫Song元件，將items傳入給他
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

            Row (
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(80.dp)
            ) {
                //Icon按鈕，功能是刪除所以Icon使用垃圾桶並設置紅色
                IconButton(
                    onClick = {
                        /*coroutineScope.launch{
                            Log.d("click", "IconButton CLick")
                            viewModel?.deleteSong(items)
                        }*/
                    },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "",
                        tint = Color.Black
                    )
                }

                //Icon按鈕，功能是刪除所以Icon使用垃圾桶並設置紅色
                IconButton(
                    onClick = {
                        /*coroutineScope.launch{
                            Log.d("click", "IconButton CLick")
                            viewModel?.deleteSong(items)
                        }*/
                    },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "",
                        tint = Color.Red
                    )
                }

                //Icon按鈕，功能是刪除所以Icon使用垃圾桶並設置紅色
                IconButton(
                    onClick = {
                        /*coroutineScope.launch{
                            Log.d("click", "IconButton CLick")
                            viewModel?.deleteSong(items)
                        }*/
                    },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}


@Composable
fun AppNavHost(navHostController: NavHostController, viewModel: MyViewModel?, items: List<Song>){
    NavHost(navController = navHostController, startDestination = "ViewPlayList") {
        composable("ViewPlayList"){ ViewPlayList(navController = navHostController)}
        composable("EditPlayList"){ EditPlayList(navController = navHostController, items = items, viewModel = viewModel)}
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewMyapp(){
    MyApp(null, null)
}