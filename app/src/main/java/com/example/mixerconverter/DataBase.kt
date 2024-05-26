package com.example.mixerconverter

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//定義資料表
@Entity(tableName = "MyPlayList")
data class Song(
    @PrimaryKey @ColumnInfo(name = "Song_id") val songId: String,
    //@ColumnInfo(name = "Song_url") val SongURL: String,
    @ColumnInfo(name = "Song_Img") val songImg: String,
    @ColumnInfo(name = "Song_name") val songName: String,
)

//DAO接口
@Dao
interface SongDao{
    @Query("SELECT * FROM MyPlayList")
    fun getAll(): Flow<List<Song>>

    @Query("SELECT * FROM MyPlayList WHERE Song_id = :id")
    fun getSongid(id: String): Flow<List<Song>>

    @Query("SELECT * FROM MyPlayList WHERE Song_Img = :songImg")
    fun getSongImg(songImg: String): Flow<List<Song>>

    @Query("SELECT * FROM MyPlayList WHERE Song_name = :songName")
    fun getSongName(songName: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)
}

//構造子
@Database(entities = [Song::class], version = 1)
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