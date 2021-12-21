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
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.realityexpander.vinder.R

const val TAG = "StartupActivity"

class StartupActivity : AppCompatActivity() {

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