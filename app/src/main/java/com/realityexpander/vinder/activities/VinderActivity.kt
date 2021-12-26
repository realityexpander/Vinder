package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.realityexpander.vinder.HostContextI
import com.realityexpander.vinder.R
import com.realityexpander.vinder.databinding.ActivityVinderBinding
import com.realityexpander.vinder.fragments.BaseFragment
import com.realityexpander.vinder.fragments.MatchesFragment
import com.realityexpander.vinder.fragments.ProfileFragment
import com.realityexpander.vinder.fragments.SwipeFragment
import com.realityexpander.vinder.utils.VINDER_ACTIVITY_SELECTED_TAB_POSITION

// Firebase console:
//   https://console.firebase.google.com/u/1/project/vinder-dating-app/database/vinder-dating-app-default-rtdb/data
// Database:
//   https://console.firebase.google.com/u/1/project/vinder-dating-app/database/vinder-dating-app-default-rtdb/data

// Tab & Fragment Id's
private enum class Tab {
    PROFILE_TAB,
    SWIPE_TAB,
    MATCHES_LIST_TAB,
}

data class VinderTabs (
    val profileTab: TabLayout.Tab = TabLayout.Tab(),
    val swipeTab: TabLayout.Tab= TabLayout.Tab(),
    val matchesTab: TabLayout.Tab = TabLayout.Tab()
)

class VinderActivity : AppCompatActivity(), HostContextI {

    private lateinit var bind: ActivityVinderBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid

    private var profileFragment: ProfileFragment? = null
    private var swipeFragment: SwipeFragment? = null
    private var matchesFragment: MatchesFragment? = null

    private var vinderTabs = VinderTabs()

    companion object {
        fun newIntent(context: Context?) = Intent(context, VinderActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityVinderBinding.inflate(layoutInflater)
        setContentView(bind.root)

        if (userId.isNullOrEmpty()) {
            onSignout()
        }

        // Setup tabs
        vinderTabs = setupVinderTabs()

        bind.navigationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                onTabSelected(tab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab) {
                    vinderTabs.profileTab -> {
                        if (profileFragment == null) {
                            profileFragment = ProfileFragment()
                        }
                        replaceFragment(profileFragment!!)
                    }
                    vinderTabs.swipeTab -> {
                        if (swipeFragment == null) {
                            swipeFragment = SwipeFragment()
                            swipeFragment!!.onUpdateUI()
                        }
                        replaceFragment(swipeFragment!!)
                    }
                    vinderTabs.matchesTab -> {
                        if (matchesFragment == null) {
                            matchesFragment = MatchesFragment()
                            matchesFragment!!.onUpdateUI()
                        }
                        replaceFragment(matchesFragment!!)
                    }
                }
            }
        })

        bind.navigationTabs.setTabIconTintResource(R.color.tab_item) // tint the icons black and red
        vinderTabs.profileTab.select()
    }

    private fun setupVinderTabs(): VinderTabs {
        val profileTab = bind.navigationTabs.newTab()
        val swipeTab = bind.navigationTabs.newTab()
        val matchesTab = bind.navigationTabs.newTab()

        profileTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_profile)
        swipeTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_swipe)
        matchesTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_matches)

        bind.navigationTabs.addTab(profileTab)
        bind.navigationTabs.addTab(swipeTab)
        bind.navigationTabs.addTab(matchesTab)

        return VinderTabs(profileTab, swipeTab, matchesTab)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSignout() {
        firebaseAuth.signOut()
        startActivity(StartupActivity.newIntent(this))
        finish()
    }

    override fun onProfileComplete() {
        vinderTabs.swipeTab.select()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // println("onSaveInstanceState for VinderActivity")

        outState.apply {
            putInt(VINDER_ACTIVITY_SELECTED_TAB_POSITION, bind.navigationTabs.selectedTabPosition)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for VinderActivity")

        savedInstanceState.apply {
            val selectedTabPosition = getInt(VINDER_ACTIVITY_SELECTED_TAB_POSITION)

            vinderTabs.profileTab.select()
            when (Tab.values()[selectedTabPosition]) {
                Tab.PROFILE_TAB -> vinderTabs.profileTab.select()
                Tab.SWIPE_TAB -> vinderTabs.swipeTab.select()
                Tab.MATCHES_LIST_TAB -> vinderTabs.matchesTab.select()
            }
        }
    }

    // After process death recovery fragment creation, update the fragment vars
    override fun onAndroidFragmentCreated(fragment: BaseFragment) {

        // note: fragment type is created inside the fragment upon process death recovery
        when (fragment) {
            is ProfileFragment -> profileFragment = fragment
            is SwipeFragment -> swipeFragment = fragment
            is MatchesFragment -> matchesFragment = fragment
        }
    }


}