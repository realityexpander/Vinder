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

// Firebase console
// https://console.firebase.google.com/u/1/project/vinder-dating-app/database/vinder-dating-app-default-rtdb/data

// Tab & Fragment Id's
private enum class FragmentId {
    PROFILE,
    SWIPE,
    MATCHES_LIST,
}

data class VinderTabLayout (
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

    private var tabLayout = VinderTabLayout()

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
        tabLayout = setupVinderTabs()

        bind.navigationTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                onTabSelected(tab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab) {
                    tabLayout.profileTab -> {
                        if (profileFragment == null) {
                            profileFragment = ProfileFragment()
                        }
                        replaceFragment(profileFragment!!)
                    }
                    tabLayout.swipeTab -> {
                        if (swipeFragment == null) {
                            swipeFragment = SwipeFragment()
                            swipeFragment!!.onUpdateUI()
                        }
                        replaceFragment(swipeFragment!!)
                    }
                    tabLayout.matchesTab -> {
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

        tabLayout.profileTab.select()
    }

    private fun setupVinderTabs(): VinderTabLayout {
        val profileTab = bind.navigationTabs.newTab()
        val swipeTab = bind.navigationTabs.newTab()
        val matchesTab = bind.navigationTabs.newTab()

        profileTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_profile)
        swipeTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_swipe)
        matchesTab.icon = ContextCompat.getDrawable(this, R.drawable.tab_matches)

        bind.navigationTabs.addTab(profileTab)
        bind.navigationTabs.addTab(swipeTab)
        bind.navigationTabs.addTab(matchesTab)

        return VinderTabLayout(profileTab, swipeTab, matchesTab)
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

    override fun profileComplete() {
        tabLayout.swipeTab.select()
    }

    override fun onAndroidFragmentCreated(fragment: BaseFragment) {
    }
}