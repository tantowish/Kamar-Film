package com.example.uas.admin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uas.R
import com.example.uas.database.Movies
import com.google.firebase.storage.StorageReference

class RvAdminAdapter(
    private val movieList: List<Movies>,
    private var store: StorageReference? = null,
    private val onItemClick: (Movies) -> Unit,
    private val onItemLongClick: (Movies) -> Unit
) : RecyclerView.Adapter<RvAdminAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val currentMovie = movieList[position]
        holder.title.text = currentMovie.title

        // Logging the image URL
        Log.d("RvAdminAdapter", "Loading image URL: ${currentMovie.imagePath}")

        Glide.with(holder.itemView.context)
            .load(currentMovie.imagePath)
//            .error(R.drawable.error) // Placeholder in case of an error
            .into(holder.imageRV)
    }



    override fun getItemCount(): Int {
        return movieList.size
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        //        val rating: AppCompatRatingBar = itemView.findViewById(R.id.rating)
        val imageRV: ImageView = itemView.findViewById(R.id.image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(movieList[position])
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(movieList[position])
                    true
                } else {
                    false
                }
            }
        }
    }
}