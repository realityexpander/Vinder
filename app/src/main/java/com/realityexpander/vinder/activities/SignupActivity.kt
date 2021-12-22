package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.realityexpander.vinder.R
import com.realityexpander.vinder.databinding.ActivityLoginBinding
import com.realityexpander.vinder.databinding.ActivitySignupBinding
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.DATA_USERS_COLLECTION

class SignupActivity : AppCompatActivity() {
    private lateinit var bind: ActivitySignupBinding

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(VinderActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.handlers = this

        //setup reset text-error message listeners
        setOnTextChangedListener(bind.emailET, bind.emailTIL)
        setOnTextChangedListener(bind.passwordET, bind.passwordTIL)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSignup(v: View) {
        var proceed = true

        // Form Validation
        if (bind.emailET.text.isNullOrEmpty()) {
            bind.emailTIL.error = "Email is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (bind.passwordET.text.isNullOrEmpty()) {
            bind.passwordTIL.error = "Password is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (proceed) {
            if (bind.emailET.text.toString().isNotEmpty()
                && bind.passwordET.text.toString().isNotEmpty()
            ) {
                // 1. Create user in Firebase Authentication
                firebaseAuth.createUserWithEmailAndPassword(
                    bind.emailET.text.toString(),
                    bind.passwordET.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(

                                this, "Signup error ${task.exception?.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val email = bind.emailET.text.toString()
                            val userId = firebaseAuth.currentUser?.uid ?: ""

                            // 2. Save User to DB
                            var user: User? = User(userId, "", "", email, "", "", "")
                            firebaseDatabase.child(DATA_USERS_COLLECTION)
                                .child(userId)
                                .setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this, "User saved!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this, "Signup error ${task.exception?.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    user = null
                                }
                        }
                    }
            }
        }
    }

    // To remove the error warning when user types into the fields again
    private fun setOnTextChangedListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_right)
    }

    companion object {
        fun newIntent(context: Context?) = Intent(context, SignupActivity::class.java)
    }
}