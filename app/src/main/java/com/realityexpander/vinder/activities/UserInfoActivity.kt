package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.realityexpander.vinder.databinding.ActivityUserInfoBinding
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.DATA_USERS_COLLECTION
import com.realityexpander.vinder.utils.loadUrl

class UserInfoActivity : AppCompatActivity() {
    private lateinit var bind: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val userId = intent.extras?.getString(USER_INFO_PARAM_USER_ID, "")
        if(userId.isNullOrEmpty()) {
            finish()
        }

        val userDatabase = FirebaseDatabase.getInstance()
            .reference
            .child(DATA_USERS_COLLECTION)
        userDatabase.child(userId!!).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(userDoc: DataSnapshot) {
                val user = userDoc.getValue(User::class.java)
                bind.userInfoName.text = user?.username
                bind.userInfoAge.text = user?.age
                if(user?.profileImageUrl != null) {
                    bind.userInfoProfileIV.loadUrl(user.profileImageUrl)
                }
            }
        })
    }

    companion object {
        const val USER_INFO_PARAM_USER_ID = "User id"

        fun newIntent(context: Context, userId: String?): Intent {
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra(USER_INFO_PARAM_USER_ID, userId)
            return intent
        }
    }
}