package com.realityexpander.vinder.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.realityexpander.vinder.HostContextI


abstract class BaseFragment: Fragment() {

    protected var hostContextI: HostContextI? = null

    // Get the Host Activity context when fragment attaches
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (this.host is HostContextI) {
            hostContextI = this.host as HostContextI
        } else {
            throw RuntimeException("${this.host} must implement HomeContextI")
        }
    }
}