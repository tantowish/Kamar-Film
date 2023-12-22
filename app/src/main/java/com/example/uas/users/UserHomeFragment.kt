package com.example.uas.users

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.DetailActivity
import com.example.uas.R
import com.example.uas.database.Movies
import com.example.uas.databinding.FragmentUserHomeBinding
import com.example.uas.helper.Constant
import com.example.uas.helper.sharepref
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserHomeFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val moviesCollectionRef = db.collection("movies")
    private lateinit var executorService: ExecutorService
    private lateinit var binding: FragmentUserHomeBinding
    private val movieListLiveData: MutableLiveData<List<Movies>> by lazy {
        MutableLiveData<List<Movies>>()
    }

    private val movieList = mutableListOf<Movies>() // List to store data to be displayed
    private lateinit var rvAdapter: RvUserAdapter // Adapter for RecyclerView

    lateinit var sharedPref: sharepref // Shared preferences instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences
        sharedPref = sharepref(requireContext())

        // Initialize RecyclerView adapter
        rvAdapter = RvUserAdapter(movieList) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("SELECTED_MOVIE", movie)
            startActivity(intent)

        }

        // Setup RecyclerView
        with(binding) {
            MyRecyclerView.layoutManager = GridLayoutManager(context,2)
            MyRecyclerView.adapter = rvAdapter

            // Retrieve username from shared preferences and set it to TextView
            user.text = sharedPref.getString(Constant.PREF_USERNAME)
            Log.d("Username", "Retrieved username: ${sharedPref.getString(Constant.PREF_USERNAME)}")
        }

        // Start observing movie data
        getAllMovies()
    }


    private fun getAllMovies() {
        observeMoviesChanges()
    }

    private fun observeMoviesChanges() {
        moviesCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("UserHomeFragment", "Error listening for movie changes: ", error)
                return@addSnapshotListener
            }
            val movies = snapshots?.toObjects(Movies::class.java)
            if (movies != null) {
                movieListLiveData.postValue(movies)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executorService = Executors.newSingleThreadExecutor()

        // Observe movie list live data
        movieListLiveData.observe(this) { movies ->
            movieList.clear()
            movieList.addAll(movies)
            rvAdapter.notifyDataSetChanged()
        }
    }
}
