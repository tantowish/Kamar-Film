package com.example.uas.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MovieRoom::class], version = 2, exportSchema = false)
abstract class MovieRoomDatabase :RoomDatabase(){
    abstract fun movieRoomDao() : MovieRoomDao

    companion object{
        @Volatile
        private var INSTANCE:MovieRoomDatabase? = null
        fun getDatabase(context: Context): MovieRoomDatabase?{
            // misalkan instance nya masih kosong, maka kita isikan dengan database dari si movie_database ini
            if (INSTANCE == null){
                synchronized(MovieRoomDatabase::class.java){
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MovieRoomDatabase::class.java, "movie_database"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}