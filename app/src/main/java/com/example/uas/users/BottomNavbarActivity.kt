package com.example.uas.users

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.uas.R
import com.example.uas.databinding.ActivityBottomNavbarBinding
import com.example.uas.helper.sharepref

class BottomNavbarActivity : AppCompatActivity() {

    lateinit var sharedPref: sharepref
    private lateinit var binding: ActivityBottomNavbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(UserHomeFragment()) // Load default fragment

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    loadFragment(UserHomeFragment())
                    true
                }
                R.id.settingFragment -> {
                    loadFragment(UserSettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
