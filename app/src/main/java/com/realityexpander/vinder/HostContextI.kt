package com.realityexpander.vinder

import com.realityexpander.vinder.fragments.BaseFragment

interface HostContextI {
    fun onAndroidFragmentCreated(fragment: BaseFragment)
    fun onSignout()
    fun onProfileComplete()
}