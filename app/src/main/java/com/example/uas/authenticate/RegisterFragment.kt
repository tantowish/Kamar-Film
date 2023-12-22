package com.example.uas.authenticate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.uas.Notif.NotifReceiver
import com.example.uas.R
import com.example.uas.admin.DashboardActivity
import com.example.uas.databinding.FragmentRegisterBinding
import com.example.uas.users.BottomNavbarActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val db = FirebaseFirestore.getInstance()

    private val channelId = "TEST NOTIF"
    private lateinit var etUsername: EditText
    private lateinit var etSpinner: Spinner
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername = binding.username
        etEmail = binding.email
        etPassword = binding.password


        binding.btnRegist.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val role = "user"

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
        } else {
            val userData = HashMap<String, Any>()
                userData["username"] = username
                userData["password"] = password
                userData["role"] = role
                userData["email"] = email

                // Save user data in Firestore
                db.collection("users")
                    .add(userData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Registration Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHome(role)
                    }
                    .addOnFailureListener { d ->
                        Toast.makeText(
                            requireContext(),
                            "Error: ${d.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

//            notif receiver
            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            val intent = Intent(requireActivity(), NotifReceiver::class.java)
                .putExtra("MESSAGE", "Baca selengkapnya ...")
            val pendingIntent = PendingIntent.getBroadcast(
                requireActivity(),
                0,
                intent,
                flag
            )

            val channelId = "your_channel_id"  // Replace with your actual channel ID
            val builder = NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Kamar Film")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(0, "Further Information", pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Registration Successful")
                )

            val notifManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notifChannel = NotificationChannel(
                    channelId,
                    "Kamar Film",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notifManager.createNotificationChannel(notifChannel)
                notifManager.notify(0, builder.build())
            } else {
                notifManager.notify(0, builder.build())
            }



            }
        }


    private fun navigateToHome(role: String) {
        val intent = when (role) {
            "Admin" -> Intent(requireContext(), DashboardActivity::class.java)
            // Add other role-specific activities here
            else -> Intent(requireContext(), BottomNavbarActivity::class.java)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding
    }
}
