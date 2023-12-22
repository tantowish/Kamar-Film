package com.example.uas.authenticate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uas.Notif.NotifReceiver
import com.example.uas.R
import com.example.uas.admin.DashboardActivity
import com.example.uas.databinding.FragmentLoginBinding
import com.example.uas.helper.Constant
import com.example.uas.helper.sharepref
import com.example.uas.users.BottomNavbarActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPref: sharepref
    private val channelId = "TEST NOTIF"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = sharepref(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        sharedPref = sharepref(requireContext())
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val notificationManager = ContextCompat.getSystemService(requireContext(), NotificationManager::class.java)


            if (username.isEmpty() || password.isEmpty()) {
                showToast("Please fill all the fields")
            } else {
                authenticateUser(username, password)

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
                            .bigText("Login Successful")
                    )

                val notifManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notifChannel = NotificationChannel(
                        channelId,
                        "Ghibli Studio",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notifManager.createNotificationChannel(notifChannel)
                    notifManager.notify(0, builder.build())
                } else {
                    notifManager.notify(0, builder.build())
                }

            }
        }
    }

    private fun authenticateUser(username: String, password: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .whereEqualTo("password" , password)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showToast("User not found")
                    return@addOnSuccessListener
                } else {
                    val userDoc = documents.documents[0]
                    val storedPassword = userDoc.getString("password")

                    if (password == storedPassword) {
                        navigateBasedOnRole(userDoc.getString("role"), username)
                    } else {
                        showToast("Incorrect password")
                    }
                }
            }
            .addOnFailureListener {
                showToast("Error accessing the database")
                Log.d("LoginFragment", "Error: ${it.message}")
            }
    }
    private fun navigateBasedOnRole(role: String?, username: String) {
        when (role) {
            "Admin" -> {
                saveSession(username, role)
                startActivity(Intent(requireContext(), DashboardActivity::class.java))
                showToast("Admin Sign In Successfully.")
            }
            "User" -> {
                saveSession(username, role)
                startActivity(Intent(requireContext(), BottomNavbarActivity::class.java))
                showToast("User Sign In Successfully.")
            }
            else -> showToast("Invalid role")
        }
        activity?.finish()
    }



    private fun saveSession(username: String, role: String) {
        sharedPref.put(Constant.PREF_USERNAME, username)
        sharedPref.put(Constant.PREF_ROLE, role)
        sharedPref.put(Constant.PREF_IS_LOGIN, true)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding
    }
}
