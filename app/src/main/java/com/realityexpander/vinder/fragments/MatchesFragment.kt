package com.realityexpander.vinder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.realityexpander.vinder.adapters.ChatsAdapter
import com.realityexpander.vinder.databinding.FragmentMatchesBinding
import com.realityexpander.vinder.interfaces.UpdateUiI
import com.realityexpander.vinder.models.Chat
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.DATA_USERS_COLLECTION
import com.realityexpander.vinder.utils.DATA_USER_MATCH_USER_ID_TO_CHAT_IDS
import com.realityexpander.vinder.utils.isNotNullAndNotEmpty

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
    private val userId = firebaseAuth.currentUser?.uid!!
    private val userDatabase = FirebaseDatabase.getInstance()
        .reference
        .child(DATA_USERS_COLLECTION)

    private val chatsAdapter = ChatsAdapter(ArrayList())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        bind.matchesRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onResume() {
        super.onResume()

        onUpdateUI()
    }

    override fun onUpdateUI() {
        if (!isAdded) return

        chatsAdapter.clearAllChats()
        bind.progressLayout.visibility = View.VISIBLE

        // Get all the chats for this userId
        userDatabase.child(userId)
            .child(DATA_USER_MATCH_USER_ID_TO_CHAT_IDS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(matchUserIdToChatIdsDoc: DataSnapshot) {
                    if (matchUserIdToChatIdsDoc.hasChildren()) {
                        addAllChatsForUserId(userId, matchUserIdToChatIdsDoc)
                    } else {
                        showEmptyIfMatchesIsEmpty()
                        bind.progressLayout.visibility = View.GONE
                    }
                }

            })
    }

    // Add all the chats for this user
    private fun addAllChatsForUserId(userId: String, matchUserIdToChatIdsDoc: DataSnapshot) {
        matchUserIdToChatIdsDoc.children.forEach { matchUserIdToChatId ->
            val matchUserId = matchUserIdToChatId.key
            val chatId = matchUserIdToChatId.value.toString()

            if (matchUserId.isNotNullAndNotEmpty()) {
                // Get the info for the matched user for this chatId
                userDatabase.child(matchUserId!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {}

                        override fun onDataChange(matchUserDoc: DataSnapshot) {
                            val matchUser = matchUserDoc.getValue(User::class.java)

                            // Add the matched user chat to the Chat list
                            if (matchUser != null) {
                                val chat = Chat(
                                    chatId,
                                    userId,
                                    matchUser.uid,
                                    matchUser.username,
                                    matchUser.profileImageUrl
                                )
                                chatsAdapter.addElement(chat)
                                showEmptyIfMatchesIsEmpty()
                            }
                            bind.progressLayout.visibility = View.GONE
                        }
                    })
            }
        }
        bind.progressLayout.visibility = View.GONE
    }

    // Show there are no more users to swipe
    private fun showEmptyIfMatchesIsEmpty() {
        if (chatsAdapter.isChatsEmpty()) {
            bind.noMatchesLayout.visibility = View.VISIBLE
        } else {
            bind.noMatchesLayout.visibility = View.GONE
        }
    }
}