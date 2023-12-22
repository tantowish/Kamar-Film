package com.example.uas.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "movie_table")
data class MovieRoom(
    @PrimaryKey var id: String = "",
    var imagePath: String = "",
    var title: String = "",
    var year: String = "",
    var description: String = "",
//    var rating: String = "",
)