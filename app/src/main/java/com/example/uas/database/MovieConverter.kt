package com.example.uas.database

object MovieConverter {
    fun convertMovieListToRoom(movieList: List<Movies>?): List<MovieRoom> {
        return movieList?.mapNotNull { movie ->
            // Map and filter out null results
            convertMovieToRoom(movie)
        } ?: emptyList()
    }

    private fun convertMovieToRoom(movie: Movies?): MovieRoom? {
        return movie?.let {
            MovieRoom(
                id = it.id,
                title = it.title,
                description = it.description,
                imagePath = it.imagePath
            )
        }
    }

    fun convertRoomMovieListToMovie(roomMovieList: List<MovieRoom>?): List<Movies> {
        return roomMovieList?.mapNotNull { roomMovie ->
            // Map and filter out null results
            convertRoomMovieToMovie(roomMovie)
        } ?: emptyList()
    }

    private fun convertRoomMovieToMovie(roomMovie: MovieRoom?): Movies? {
        return roomMovie?.let {
            Movies(
                id = it.id,
                title = it.title,
                description = it.description,
                imagePath = it.imagePath
            )
        }
    }


}