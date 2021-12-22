package com.realityexpander.vinder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.realityexpander.vinder.databinding.FragmentSwipeBinding
import com.realityexpander.vinder.interfaces.UpdateUiI

class SwipeFragment : BaseFragment(), UpdateUiI {

    private var _bind: FragmentSwipeBinding? = null
    private val bind: FragmentSwipeBinding
        get() = _bind!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _bind = FragmentSwipeBinding.inflate(inflater, container, false)
        return bind.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.apply {
            // After process death, pass this System-created fragment to HostContext
            hostContextI?.onAndroidFragmentCreated(this@SwipeFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }
    }

    override fun onUpdateUI() {
    }

}