package com.realityexpander.vinder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.realityexpander.vinder.R
import com.realityexpander.vinder.adapters.CardsAdapter
import com.realityexpander.vinder.databinding.FragmentSwipeBinding
import com.realityexpander.vinder.interfaces.UpdateUiI
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.*

class SwipeFragment : BaseFragment(), UpdateUiI {

    private var _bind: FragmentSwipeBinding? = null
    private val bind: FragmentSwipeBinding
        get() = _bind!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid!!
    private val userDatabase = FirebaseDatabase.getInstance()
        .reference
        .child(DATA_USERS_COLLECTION)

    private var cardsAdapter: ArrayAdapter<User>? = null
    private var rowItems = ArrayList<User>()

    private var preferredGender: String? = null
    private var userName: String? = null
    private var imageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Get User profile
        userDatabase.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java) ?: return

                    preferredGender = user.preferredGender
                    userName = user.username
                    imageUrl = user.profileImageUrl

                    onUpdateUI()
                }
            })

        // Setup the cards adapter
        cardsAdapter = CardsAdapter(context, R.layout.item_card, rowItems)
        bind.frame.adapter = cardsAdapter
        setupFlingAdapter()

        // Setup Like button
        bind.likeButton.setOnClickListener {
            if (rowItems.isNotEmpty()) {
                bind.frame.topCardListener.selectRight()
            }
        }

        // Setup dislike button
        bind.dislikeButton.setOnClickListener {
            if (rowItems.isNotEmpty()) {
                bind.frame.topCardListener.selectLeft()
            }
        }
    }

    private fun setupFlingAdapter() {
        bind.frame.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            override fun removeFirstObjectInAdapter() {
                rowItems.removeAt(0)
                cardsAdapter?.notifyDataSetChanged()
            }

            override fun onLeftCardExit(selectedUserItem: Any?) {
                val user = selectedUserItem as User

                userDatabase.child(user.uid.toString())
                    .child(DATA_USER_SWIPES_LEFT_USER_ID)
                    .child(userId)
                    .setValue(true)
            }

            override fun onRightCardExit(selectedUserItem: Any?) {
                val selectedUser = selectedUserItem as User
                val selectedUserId = selectedUser.uid

                if (selectedUserId.isNotNullAndNotEmpty()) {
                    addMatch(userId, selectedUserId!!, selectedUser)
                }
            }

            override fun onAdapterAboutToEmpty(p0: Int) {}
            override fun onScroll(p0: Float) {}
        })

        // No taps on the frame
        bind.frame.setOnItemClickListener { position, data -> }
    }

    private fun addMatch(
        userId: String,
        selectedUserId: String,
        selectedUser: User
    ) {
        userDatabase.child(userId)
            .child(DATA_USER_SWIPES_RIGHT_USER_ID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild(selectedUserId)) {
                        Toast.makeText(context, "Match!", Toast.LENGTH_SHORT).show()

//                        val chatKey = chatDatabase.push().key

//                        if (chatKey != null) {
//                            userDatabase.child(userId)
//                                .child(DATA_USER_SWIPES_RIGHT_USER_ID)
//                                .child(selectedUserId)
//                                .removeValue()
//                            userDatabase.child(userId)
//                                .child(DATA_USER_MATCHES_USER_ID)
//                                .child(selectedUserId)
//                                .setValue(chatKey)
//                            userDatabase.child(selectedUserId)
//                                .child(DATA_USER_MATCHES_USER_ID)
//                                .child(userId)
//                                .setValue(chatKey)

//                            chatDatabase.child(chatKey).child(userId).child(DATA_NAME)
//                                .setValue(userName)
//                            chatDatabase.child(chatKey).child(userId).child(DATA_IMAGE_URL)
//                                .setValue(imageUrl)
//
//                            chatDatabase.child(chatKey).child(selectedUserId).child(DATA_NAME)
//                                .setValue(selectedUser.name)
//                            chatDatabase.child(chatKey).child(selectedUserId).child(DATA_IMAGE_URL)
//                                .setValue(selectedUser.imageUrl)
//                        }
                    } else {
                        userDatabase.child(selectedUserId).child(DATA_USER_SWIPES_RIGHT_USER_ID)
                            .child(userId)
                            .setValue(true)
                    }
                }
            })
    }

    override fun onUpdateUI() {
        if (!isAdded) return

        bind.noUsersLayout.visibility = View.GONE
        bind.progressLayout.visibility = View.VISIBLE

        // Query all users of the preferred gender
        userDatabase.orderByChild(DATA_USER_GENDER_PREFERENCE)
            .equalTo(preferredGender)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(queryResult: DataSnapshot) {
                    queryResult.children.forEach { child ->
                        val swipeUser = child.getValue(User::class.java)

                        if (swipeUser != null) {
                            var showUser = true

                            // has this swipeUser already been swiped?
                            if (child.child(DATA_USER_SWIPES_LEFT_USER_ID)
                                    .hasChild(userId)
                                || child.child(DATA_USER_SWIPES_RIGHT_USER_ID)
                                    .hasChild(userId)
                                || child.child(DATA_USER_MATCHES_USER_ID)
                                    .hasChild(userId)
                            ) {
                                showUser = false
                            }

                            if (showUser) {
                                rowItems.add(swipeUser)
                                cardsAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    bind.progressLayout.visibility = View.GONE

                    // Notify there are no more users to swipe
                    if (rowItems.isEmpty()) {
                        bind.noUsersLayout.visibility = View.VISIBLE
                    }
                }
            })
    }

}