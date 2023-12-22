package com.example.uas.database


import androidx.room.*
@Dao
interface MovieRoomDao {
    // fungsi movieRoomDao sebagai aks, setiap akses yang terjadi di database, ini harus melalui perantara interface ini
    // jadi setiap crud nya di harus melalui ini dulu

    // penambahan data movie
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(movies: List<MovieRoom>)

    // get data list movie, melakukan select list movie melalui movie_table dari data class Note
    @Query("SELECT * FROM movie_table")
    fun getAllMovies(): List<MovieRoom>

    // Hapus semua data dari tabel movie_table
    @Query("DELETE FROM movie_table")
    fun deleteAll()
}