package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.get
import com.google.firebase.auth.FirebaseAuth
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.realityexpander.vinder.R

const val TAG = "StartupActivity"

class StartupActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(VinderActivity.newIntent(this))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
    }

    fun onLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun onSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    companion object {
        fun newIntent(context: Context?) = Intent(context, StartupActivity::class.java)
    }


}