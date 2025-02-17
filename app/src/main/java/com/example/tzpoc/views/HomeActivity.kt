package com.example.tzpoc.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.tzpoc.ContainerDeMappingActivity
import com.example.tzpoc.R
import com.example.tzpoc.RfidMappingActivity
import com.example.tzpoc.databinding.ActivityHomeBinding
import com.example.tzpoc.helper.SessionManager

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_home)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        session = SessionManager(this)
        val username = session.getUserDetails()["userName"]
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }
        binding.mcvMapDeMap.setOnClickListener{
            startActivity(Intent(this@HomeActivity,ContainerMappingActivity::class.java))
        }
        binding.imgTagNewMapping.setOnClickListener{
            startActivity(Intent(this@HomeActivity,ContainerDeMappingActivity::class.java))
        }
        binding.mcvRfidAssetMapping.setOnClickListener{
            startActivity(Intent(this@HomeActivity,EntryActivity::class.java))
        }

        binding.mcvTagMerge.setOnClickListener{
            startActivity(Intent(this@HomeActivity,ExitActivity::class.java))
        }
        binding.mcvNewCard.setOnClickListener{
            startActivity(Intent(this@HomeActivity,RfidMappingActivity::class.java))
        }
    }

private fun logout(){
    session.logoutUser()
    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
    finish()
}
private fun showLogoutDialog() {
    val builder = AlertDialog.Builder(this)

    builder.setTitle("Logout")
        .setMessage("Are you sure you want to log out?")
        .setPositiveButton("Yes") { dialog, which ->
            logout()
        }
        .setNegativeButton("Cancel") { dialog, which ->

            dialog.dismiss()
        }
        .setCancelable(false)

    val dialog = builder.create()
    dialog.show()
}
}