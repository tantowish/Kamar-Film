package com.example.uas.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.uas.authenticate.AuthActivity
import com.example.uas.database.Movies
import com.example.uas.DetailActivity
import com.example.uas.databinding.ActivityDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    private val db = FirebaseFirestore.getInstance()
    private val reportCollectionRef = db.collection("movies")
    private lateinit var executorService: ExecutorService
    private val movieListLiveData: MutableLiveData<List<Movies>> by lazy {
        MutableLiveData<List<Movies>>()
    }
    private val listMovies = mutableListOf<Movies>() // Tambahkan list untuk menyimpan data yang akan ditampilkan
    private lateinit var rvAdapter: RvAdminAdapter // Deklarasikan adapter di sini
    private var store: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executorService = Executors.newSingleThreadExecutor()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvAdapter = RvAdminAdapter(listMovies,
            onItemClick = { movie ->
                val dialog = AlertDialog.Builder(this@DashboardActivity)
                    .setTitle("Choose an action")

                    .setPositiveButton("Update") { _, _ ->
                        executorService.execute {
                        val position = listMovies.indexOf(movie)
                        val selectedMovies = listMovies[position] // posisi gambar yang diklik
                        val intent = Intent(this@DashboardActivity, EditActivity::class.java)
                        intent.putExtra("SELECTED_MOVIES", selectedMovies)
                        startActivity(intent)
    //                    startActivityForResult(intent, 2)
                       }
                    }

                    // Add Delete button
                    .setNegativeButton("Delete") { _, _ ->
                        // Call deleteMovies function
                        deleteMovies(movie)
                    }
                    .create()

                // Show the dialog
                dialog.show()
            },

            onItemLongClick = { movie -> // Create an AlertDialog
                executorService.execute {
                    val position = listMovies.indexOf(movie)
                    val selectedMovie = listMovies[position] // Position of the clicked item
                    val intent = Intent(this@DashboardActivity, DetailActivity::class.java)
                    intent.putExtra("SELECTED_MOVIE", selectedMovie)
                    startActivity(intent)
                    // Uncomment the next line if you need to start the activity for result
                    // startActivityForResult(intent, 2)
                }
            }
        )

        binding.MyRecyclerView.apply {
            layoutManager = GridLayoutManager(context , 2)
            adapter = rvAdapter

        }

        binding.addBtn.setOnClickListener{
            val intent = Intent(this@DashboardActivity, CreateActivity::class.java)
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener{
            val intent = Intent(this@DashboardActivity, AuthActivity::class.java)
            startActivity(intent)
        }

        observeMovies()
        getAllMovies()

        Log.d("MainAdminActivity", "RecyclerView initialization")

    }

    private fun getAllMovies() {
        observeMoviesChanges()
    }
    private fun observeMovies() {
        movieListLiveData.removeObservers(this) // Hapus observer sebelum menambahkan yang baru
        movieListLiveData.observe(this) { report->
            listMovies.clear()
            listMovies.addAll(report)
            runOnUiThread {
                rvAdapter.notifyDataSetChanged()
            }
        }
        Log.d("observe", "masuk")
    }
    private fun observeMoviesChanges() {
        reportCollectionRef.addSnapshotListener { snapshots, error->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val reports = snapshots?.toObjects(Movies::class.java)
            if (reports != null) {
                movieListLiveData.postValue(reports)
            }
        }
    }
    private fun deleteMovies(movie: Movies) {
        if (movie.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: movie ID is empty!")
            return
        }
        reportCollectionRef.document(movie.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting movie: ", it)
            }
    }
}