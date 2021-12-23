package com.realityexpander.vinder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.realityexpander.vinder.databinding.FragmentProfileBinding
import com.realityexpander.vinder.interfaces.UpdateUiI
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.DATA_USERS_COLLECTION
import com.realityexpander.vinder.utils.PREFERENCE_GENDER_FEMALE
import com.realityexpander.vinder.utils.PREFERENCE_GENDER_MALE
import com.realityexpander.vinder.utils.loadUrl

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : BaseFragment(), UpdateUiI {

    private var _bind: FragmentProfileBinding? = null
    private val bind: FragmentProfileBinding
        get() = _bind!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private val userDatabase =  FirebaseDatabase.getInstance()
        .reference
        .child(DATA_USERS_COLLECTION)
        .child(userId!!)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _bind = FragmentProfileBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.apply {
            // After process death, pass this System-created fragment to HostContext
            hostContextI?.onAndroidFragmentCreated(this@ProfileFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }

        onUpdateUI()
    }

    override fun onUpdateUI() {
        if(!isAdded) return

        bind.progressLayout.visibility = View.VISIBLE

        userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                bind.progressLayout.visibility = View.GONE
            }

            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (isAdded) {
                    val user = userSnapshot.getValue(User::class.java)

                    bind.nameEt.setText(user?.username, TextView.BufferType.EDITABLE)
                    bind.emailEt.setText(user?.email, TextView.BufferType.EDITABLE)
                    bind.ageEt.setText(user?.age, TextView.BufferType.EDITABLE)
                    if (user?.gender == PREFERENCE_GENDER_MALE) {
                        bind.radioMan1.isChecked = true
                    }
                    if (user?.gender == PREFERENCE_GENDER_FEMALE) {
                        bind.radioWoman1.isChecked = true
                    }
                    if (user?.preferredGender == PREFERENCE_GENDER_MALE) {
                        bind.radioMan2.isChecked = true
                    }
                    if (user?.preferredGender == PREFERENCE_GENDER_FEMALE) {
                        bind.radioWoman2.isChecked = true
                    }
                    if(!user?.profileImageUrl.isNullOrEmpty()) {
                        bind.photoIv.loadUrl(user?.profileImageUrl!!)
                    }
                    bind.progressLayout.visibility = View.GONE
                }
            }

        })
    }
}