package com.strink.sociallogins

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.strink.sociallogins.databinding.ActivityProfilePageBinding

class ProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.buttonLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null) {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        } else {
            Picasso.get().load(intent.getParcelableExtra("ProfilePic"))
                .error(R.drawable.ic_launcher_foreground).into(binding.profileImageView);
            binding.nameTextView.text = intent.getStringExtra("Name")
            binding.emailTextView.text = intent.getStringExtra("Email")
        }
    }
}