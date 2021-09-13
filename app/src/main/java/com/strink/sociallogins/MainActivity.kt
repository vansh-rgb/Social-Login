package com.strink.sociallogins

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.strink.sociallogins.databinding.ActivityMainBinding
import okhttp3.internal.Internal.instance
import kotlin.math.log2

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
        private const val TAG2 = "FacebookLogin"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        val currentUser = firebaseAuth.currentUser
        checkUser()

        binding.signInButton.setOnClickListener {
            Log.d(TAG,"onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        callbackManager = CallbackManager.Factory.create()
        binding.facebookLogin.setReadPermissions("email","public_profile")
        binding.facebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                TODO("Not yet implemented")
            }

            override fun onError(error: FacebookException?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser!=null) {
            val firebaseUser = firebaseAuth.currentUser
            val uid = firebaseAuth.uid
            val email = firebaseUser?.email
            val name = firebaseUser?.displayName
            val pic = firebaseUser?.photoUrl
            val intent = Intent(this,ProfilePage::class.java)
            intent.putExtra("Name",name)
            intent.putExtra("Email",email)
            intent.putExtra("ProfilePic",pic)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN) {
            Log.d(TAG,"onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google Sign in success
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch(e: Exception) {
                //failed Google Sign In
                Log.d(TAG,"onActivityResult: ${e.message}")
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG2, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG2, "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    val pic = user?.photoUrl
                    Log.d(TAG, "handleFacebookAccessToken: Pic: $pic")
                    println("$pic")
                    Toast.makeText(this, "Facebook Did it Pic: $pic", Toast.LENGTH_SHORT).show()
                    checkUser()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG2, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser

    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth google account")
        val credentials = GoogleAuthProvider.getCredential(account?.idToken,null)
        firebaseAuth.signInWithCredential(credentials)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Logged In")

                val firebaseUser = firebaseAuth.currentUser

                val uid = firebaseAuth.uid
                val email = firebaseUser?.email
                val name = firebaseUser?.displayName
                val pic = firebaseUser?.photoUrl

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email , Uid: $uid, Name: $name")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: PhotoUrl: $pic")

                val intent = Intent(this,ProfilePage::class.java)
                intent.putExtra("Name",name)
                intent.putExtra("Email",email)
                intent.putExtra("ProfilePic",pic)
                startActivity(intent)
                finish()

                if(authResult.additionalUserInfo?.isNewUser == true) {
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account Created")
                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Account Existed", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener { e->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed Due to ${e.message}")
                Toast.makeText(this, "Login Failed... Error", Toast.LENGTH_SHORT).show()
            }
    }


}