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
    private val chatDatabase = FirebaseDatabase.getInstance()
        .reference
        .child(DATA_CHATS_COLLECTION)

    private var cardsAdapter: ArrayAdapter<User>? = null
    private var rowItems = ArrayList<User>()

    private var preferredGender: String? = null
    private var username: String? = null
    private var userProfileImageUrl: String? = null

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

        // Get Current User profile
        userDatabase.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(currentUser: DataSnapshot) {
                    val user = currentUser.getValue(User::class.java) ?: return

                    preferredGender = user.preferredGender
                    username = user.username
                    userProfileImageUrl = user.profileImageUrl

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

                // Add the swipedLeft userId to the current user's list of swipedLeft userIds
                userDatabase.child(user.uid.toString())
                    .child(DATA_USER_SWIPE_LEFT_USER_IDS)
                    .child(userId)
                    .setValue(true)

                // Notify there are no more users to swipe
                if (rowItems.isEmpty()) {
                    bind.noUsersLayout.visibility = View.VISIBLE
                }
            }

            override fun onRightCardExit(swipedRightUserItem: Any?) {
                val swipedRightUser = swipedRightUserItem as User
                val swipedRightUserId = swipedRightUser.uid

                if (swipedRightUserId.isNotNullAndNotEmpty()) {
                    addMatch(userId, swipedRightUserId!!, swipedRightUser)
                }

                // Notify there are no more users to swipe
                if (rowItems.isEmpty()) {
                    bind.noUsersLayout.visibility = View.VISIBLE
                }
            }

            override fun onAdapterAboutToEmpty(p0: Int) {}
            override fun onScroll(scrollAmount: Float) {}
        })

        // No taps on the frame
        bind.frame.setOnItemClickListener { position, data -> }
    }

    private fun addMatch(
        currentUserId: String,
        swipedRightUserId: String,
        swipedRightUser: User
    ) {
        userDatabase.child(currentUserId)
            .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(swipeRightUserIds: DataSnapshot) {
                    if (swipeRightUserIds.hasChild(swipedRightUserId)) {
                        Toast.makeText(context, "Match!", Toast.LENGTH_SHORT).show()

                        // create new matchChat document
                        val matchChatId = chatDatabase.push().key

                        if (matchChatId != null) {
                            // -----------------------------------------------------
                            // ------ Setup the match for the matched users --------
                            // -----------------------------------------------------
                            // Add the swipedRight userId to the current user's list of swipedRight userIds
                            userDatabase.child(currentUserId)
                                .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                                .child(swipedRightUserId)
                                .removeValue()

                            // Add the swipedRight userId to the current user's list of matched userIds
                            userDatabase.child(currentUserId)
                                .child(DATA_USER_MATCH_USER_IDS)
                                .child(swipedRightUserId)
                                .setValue(matchChatId)
                            // Add the current userId to the swipedRight user's list of matched userIds
                            userDatabase.child(swipedRightUserId)
                                .child(DATA_USER_MATCH_USER_IDS)
                                .child(currentUserId)
                                .setValue(matchChatId)

                            // --------------------------------
                            // ----- Setup the match chat -----
                            // --------------------------------
                            // Add the current user's username to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(currentUserId)
                                .child(DATA_USER_USERNAME)
                                .setValue(username)
                            // Add the current user's profile image to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(currentUserId)
                                .child(DATA_USER_PROFILE_IMAGE_URL)
                                .setValue(userProfileImageUrl)

                            // Add the swipedRight user's username to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(swipedRightUserId)
                                .child(DATA_USER_USERNAME)
                                .setValue(swipedRightUser.username)
                            // Add the swipedRight user's profile image to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(swipedRightUserId)
                                .child(DATA_USER_PROFILE_IMAGE_URL)
                                .setValue(swipedRightUser.profileImageUrl)
                        }
                    } else {
                        // Add the current userId to the swipedRight user's list of swiped right userIds
                        userDatabase.child(swipedRightUserId)
                            .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                            .child(currentUserId)
                            .setValue(true)
                    }
                }
            })
    }

    override fun onUpdateUI() {
        if (!isAdded) return

        bind.noUsersLayout.visibility = View.GONE
        bind.progressLayout.visibility = View.VISIBLE

        rowItems.clear()

        // Query all users of the preferred gender
        userDatabase.orderByChild(DATA_USER_GENDER)
            .equalTo(preferredGender)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(queryResult: DataSnapshot) {
                    queryResult.children.forEach { child ->
                        val swipeUser = child.getValue(User::class.java)

                        if (swipeUser != null) {
                            var showUser = true

                            // has this swipeUser already been swiped?
                            if (child.child(DATA_USER_SWIPE_LEFT_USER_IDS)
                                    .hasChild(userId)
                                || child.child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                                    .hasChild(userId)
                                || child.child(DATA_USER_MATCH_USER_IDS)
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