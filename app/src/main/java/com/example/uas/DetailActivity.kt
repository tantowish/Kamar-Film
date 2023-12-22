package com.example.uas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.uas.database.Movies
import com.example.uas.databinding.ActivityDetailBinding
import com.example.uas.helper.Constant
import com.example.uas.helper.sharepref
import com.example.uas.users.BottomNavbarActivity
import com.google.firebase.firestore.FirebaseFirestore

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val db = FirebaseFirestore.getInstance()
private lateinit var sharedPref: sharepref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val movie = intent.getParcelableExtra<Movies>("SELECTED_MOVIE")

        // Retrieve the movie title passed from the previous activity
        val movieTitle = movie?.title
        binding.detailTitle.text = movieTitle

        if (movieTitle != null) {
            db.collection("movies")
                .whereEqualTo("title", movieTitle)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val detailTitle = document.getString("title")
                            val detailYear = document.getString("year")
                            val detailDescription = document.getString("description")
                            val detailImage = document.getString("imagePath")

                            Glide.with(this)
                                .load(detailImage)
                                .into(binding.imageDetail)

                            binding.detailTitle.text = detailTitle
                            binding.detailYear.text = detailYear
                            binding.detailDescription.text = detailDescription
                        }
                    } else {
                        Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "No movie title provided", Toast.LENGTH_SHORT).show()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}

