package com.realityexpander.vinder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.realityexpander.vinder.R
import com.realityexpander.vinder.databinding.FragmentMatchesBinding
import com.realityexpander.vinder.databinding.FragmentProfileBinding
import com.realityexpander.vinder.interfaces.UpdateUiI
import com.realityexpander.vinder.utils.DATA_CHATS_COLLECTION
import com.realityexpander.vinder.utils.DATA_USERS_COLLECTION

/**
 * A simple [Fragment] subclass.
 * Use the [MatchesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchesFragment : BaseFragment(), UpdateUiI {

    private var _bind: FragmentMatchesBinding? = null
    private val bind: FragmentMatchesBinding
        get() = _bind!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userDatabase = FirebaseDatabase.getInstance().reference.child(DATA_USERS_COLLECTION)
    private val chatDatabase = FirebaseDatabase.getInstance().reference.child(DATA_CHATS_COLLECTION)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _bind = FragmentMatchesBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.apply {
            // After process death, pass this System-created fragment to HostContext
            hostContextI?.onAndroidFragmentCreated(this@MatchesFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }
    }

    override fun onUpdateUI() {
    }
}