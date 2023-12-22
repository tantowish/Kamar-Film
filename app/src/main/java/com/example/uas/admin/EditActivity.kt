package com.example.uas.admin

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.uas.database.Movies
import com.example.uas.databinding.ActivityEditBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    private var imageUri: Uri? = null
    private var store: StorageReference? = null
    private var updateId: String = ""
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedMovie = intent.getParcelableExtra<Movies>("SELECTED_MOVIES")
        selectedMovie?.let {
            updateWithSelectedMovie(selectedMovie)
            updateId = selectedMovie.id
        }

        with(binding) {
            imageMovie.setOnClickListener {
                selectImage()
            }
            ButtonSubmitMovie.setOnClickListener {
                if (imageUri != null) {
                    // Jika gambar diganti, upload gambar dan update film
                    uploadImageAndUpdateMovie(imageUri!!) { downloadUrl ->
                        updateMovie(
                            Movies(
                                imagePath = downloadUrl,
                                title = EditMovieTitle.text.toString(),
                                year = EditMovieYear.text.toString(),
//                                rating = EditMovieRating.text.toString(),
                                description = EditMovieDescription.text.toString(),
                            )
                        )
                    }
                } else {
                    // Jika gambar tidak diganti, langsung update film tanpa mengunggah gambar
                    updateMovie(
                        Movies(
                            title = EditMovieTitle.text.toString(),
                            year = EditMovieYear.text.toString(),
//                            rating = EditMovieRating.text.toString(),
                            description = EditMovieDescription.text.toString(),
                        )
                    )
                }
//                notif()
                Toast.makeText(this@EditActivity, "Movie updated successfully!", Toast.LENGTH_SHORT)
                    .show()
            }

            backBtn.setOnClickListener {
                val intent = Intent(this@EditActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }

//            ButtonDeleteMovie.setOnClickListener{
//                if (updateId.isNotEmpty()) {
//                    val movieCollectionRef = db.collection("movies")
//                    val movieRef = movieCollectionRef.document(updateId)
//
//                    // Delete the document
//                    movieRef.delete()
//                        .addOnSuccessListener {
//                            Log.d("EditMoviesActivity", "Movie deleted successfully!")
//                            // (Assuming you have stored the image URLs in the "imagePath" field)
//                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(selectedMovie?.imagePath ?: "")
//                            storageRef.delete().addOnSuccessListener {
//                                Toast.makeText(this@EditMoviesActivity, "Movie deleted successfully!", Toast.LENGTH_SHORT).show()
//                                Log.d("EditMoviesActivity", "Movie image deleted successfully!")
//                            }.addOnFailureListener { e ->
//                                Toast.makeText(this@EditMoviesActivity, "Error deleting movie image", Toast.LENGTH_SHORT).show()
//                                Log.w("EditMoviesActivity", "Error deleting movie image", e)
//                            }
//
//                            val intent = Intent(this@EditMoviesActivity, MainActivityAdmin::class.java)
//                            startActivity(intent)
//                            finish()
//                        }
//                        .addOnFailureListener { e ->
//                            Log.w("EditMoviesActivity", "Error deleting movie", e)
//                        }
//                } else {
//                    Log.e("EditMoviesActivity", "Error deleting movie: updateId is empty!")
//                }
//            }
        }
    }

    // buat ngambil data setelah dipencet
    private fun updateWithSelectedMovie(selectedMovie: Movies) {
        with(binding) {
            EditMovieTitle.setText(selectedMovie.title)
            EditMovieYear.setText(selectedMovie.year)
            EditMovieDescription.setText(selectedMovie.description)
//            EditMovieRating.setText(selectedMovie.rating)

            // Load the image using Glide
            val imagePath = selectedMovie.imagePath
            if (!imagePath.isNullOrEmpty()) {
                Glide.with(this@EditActivity)
                    .load(imagePath)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(imageMovie)
            }
        }
    }

    private fun updateMovie(movie: Movies) {
        if (updateId.isNotEmpty()) {
            // Get the document reference for the report to update
            val movieCollectionRef = db.collection("movies")
            val movieRef = movieCollectionRef.document(updateId)

            val updates = mapOf(
                "imagePath" to movie.imagePath,
                "title" to movie.title,
                "director" to movie.year,
//                "rating" to movie.rating,
                "description" to movie.description,
            )
            val updatesNoPict = mapOf(
                "title" to movie.title,
                "director" to movie.year,
//                "rating" to movie.rating,
                "description" to movie.description,
            )

            // Update the document with the provided data
            if (imageUri != null) {
                movieRef.update(updates)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Movie updated successfully!")
                        val intent = Intent(this@EditActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("MainActivity", "Error updating Movie", e)
                    }
            } else {
                movieRef.update(updatesNoPict)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Movie updated successfully!")
                        val intent = Intent(this@EditActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("MainActivity", "Error updating Movie", e)
                    }

            }
        } else {
            Log.e("UpdateMovie", "Error updating Movie: updateId is empty!")
        }
    }

    // fungsi terkait image
    private fun selectImage() {
        val intentImage = Intent()
        intentImage.type = "image/*"
        intentImage.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intentImage, 100)
    }

    // preview image dgn requestCode
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 100 && data != null && data.data != null) {
            imageUri = data.data
            binding.imageMovie.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndUpdateMovie(imageUri: Uri, onSuccess: (downloadUrl: String) -> Unit) {
        val progression = android.app.ProgressDialog(this)
        progression.setTitle("Uploading Movie Image...")
        progression.show()

        val sdf = SimpleDateFormat("ddMMyyhhmmss")
        val imageName = sdf.format(System.currentTimeMillis())
        val imageExtension = ".jpg"

        val store = FirebaseStorage.getInstance().getReference("image/$imageName$imageExtension")
        store.putFile(imageUri)
            .addOnSuccessListener {
                binding.imageMovie.setImageURI(null)
                store.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Log.d("Download URL", downloadUrl)
                    onSuccess.invoke(downloadUrl)
                    progression.dismiss()
                }
            }
    }
}

//                    notifWithImage(imageUri.toString())

    // notif
//    private fun notif(){
//        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        }
//        else {
//            0
//        }
//        val intent = Intent(this, mainAdminActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            flag
//        )
//        val builder = NotificationCompat.Builder(this, channelId)
////            .setSmallIcon(R.drawable.logo_notflix_1)
//            .setContentTitle("Notflix")
//            .setContentText("Movie updated successfully!")
//            .setAutoCancel(true)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as
//                NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notifChannel = NotificationChannel(
//                channelId, // Id channel
//                "Notifku", // Nama channel notifikasi
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            with(notifManager) {
//                createNotificationChannel(notifChannel)
//                notify(notifId, builder.build())
//            }
//        }
//        else {
//            notifManager.notify(notifId, builder.build())
//        }
//    }
//
//    private fun notifWithImage(downloadUrl: String) {
//        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Download the image from Firebase Storage using Glide or any other image loading library
//        Glide.with(this@EditActivity)
//            .asBitmap()
//            .load(downloadUrl) // Use the downloadUrl here
//            .into(object : SimpleTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    // Build the notification with the downloaded image
//                    val builder = NotificationCompat.Builder(this@EditActivity, channelId)
//                        .setSmallIcon(R.drawable.logo_notflix_1)
//                        .setContentTitle("Notflix")
//                        .setContentText("Images updated successfully")
//                        .setStyle(
//                            NotificationCompat.BigPictureStyle()
//                                .bigPicture(resource)
//                        )
//                        .setAutoCancel(true)
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//                    // Notify using the NotificationManager
//                    notifManager.notify(notifId, builder.build())
//                }
//            })
//    }
//}


