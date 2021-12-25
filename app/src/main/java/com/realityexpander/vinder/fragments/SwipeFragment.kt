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

        // Block taps on the frame
        bind.frame.setOnItemClickListener { _, _ -> }
    }

    private fun addMatch(
        currentUserId: String,
        swipedRightUserId: String,
        swipedRightUser: User
    ) {
        // If the swipedRight user has also swipedRight on the currentUserId, then there is a MATCH!
        userDatabase.child(swipedRightUserId)
            .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(swipeRightUserIds: DataSnapshot) {
                    if (swipeRightUserIds.hasChild(currentUserId)) {
                        Toast.makeText(context, "Match!", Toast.LENGTH_SHORT).show()

                        // create new matchChat document
                        val matchChatId = chatDatabase.push().key

                        if (matchChatId != null) {

                            // ---------------------------------------------------------------------
                            // ------ Remove this user & matched user from swipedRight users -------
                            // ---------------------------------------------------------------------
                            // Remove the old swipedRightUserId from the currentUserId's list of swipedRight userIds
                            userDatabase.child(currentUserId)
                                .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                                .child(swipedRightUserId)
                                .removeValue()

                            // Remove the the currentUserId from the matched user's list of swipedRight userIds
                            userDatabase.child(swipedRightUserId)
                                .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                                .child(currentUserId)
                                .removeValue()

                            // --------------------------------------------------
                            // ------ Add the match for the Matched users -------
                            // --------------------------------------------------
                            // Add the swipedRightUserId to the current user's list of matched userIds
                            //   & set the MatchChatId
                            userDatabase.child(currentUserId)
                                .child(DATA_USER_MATCH_USER_ID_TO_CHAT_IDS)
                                .child(swipedRightUserId)
                                .setValue(matchChatId)

                            // Add the currentUserId to the swipedRightUserId's list of matched userIds
                            //   & set the MatchChatId
                            userDatabase.child(swipedRightUserId)
                                .child(DATA_USER_MATCH_USER_ID_TO_CHAT_IDS)
                                .child(currentUserId)
                                .setValue(matchChatId)

                            // ----------------------------------------------------
                            // ----- Add the match chat for this matched pair -----
                            // ----------------------------------------------------
                            // Add the currentUserId's username to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(currentUserId)
                                .child(DATA_USER_USERNAME)
                                .setValue(username)
                            // Add the currentUserId's profile image to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(currentUserId)
                                .child(DATA_USER_PROFILE_IMAGE_URL)
                                .setValue(userProfileImageUrl)

                            // Add the swipedRightUserId's username to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(swipedRightUserId)
                                .child(DATA_USER_USERNAME)
                                .setValue(swipedRightUser.username)
                            // Add the swipedRightUserId's profile image to the Match Chat
                            chatDatabase.child(matchChatId)
                                .child(swipedRightUserId)
                                .child(DATA_USER_PROFILE_IMAGE_URL)
                                .setValue(swipedRightUser.profileImageUrl)
                        }
                    } else {
                        // Add the swipedRight userId to the current user's list of swiped right userIds
                        userDatabase.child(currentUserId)
                            .child(DATA_USER_SWIPE_RIGHT_USER_IDS)
                            .child(swipedRightUserId)
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
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(potentialUserMatchDoc: DataSnapshot) {
                    potentialUserMatchDoc.children.forEach { potentialUserMatch ->
                        val potentialUser = potentialUserMatch.getValue(User::class.java)

                        if (potentialUser != null) {
                            var showUser = true

                            // has this swipeUser already been matched or they swiped left on this user?
                            if (potentialUserMatch.child(DATA_USER_SWIPE_LEFT_USER_IDS)
                                    .hasChild(userId)
//                                || child.child(DATA_USER_SWIPE_RIGHT_USER_IDS)
//                                    .hasChild(userId)
                                || potentialUserMatch.child(DATA_USER_MATCH_USER_ID_TO_CHAT_IDS)
                                    .hasChild(userId)
                            ) {
                                showUser = false
                            }

                            if (showUser) {
                                rowItems.add(potentialUser)
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