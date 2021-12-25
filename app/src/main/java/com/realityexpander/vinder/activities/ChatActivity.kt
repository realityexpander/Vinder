package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.realityexpander.vinder.adapters.MessagesAdapter
import com.realityexpander.vinder.databinding.ActivityChatBinding
import com.realityexpander.vinder.models.Message
import com.realityexpander.vinder.models.User
import com.realityexpander.vinder.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.contracts.ExperimentalContracts

class ChatActivity : AppCompatActivity() {

    private lateinit var bind: ActivityChatBinding

    private lateinit var chatDatabase: DatabaseReference
    private lateinit var messagesAdapter: MessagesAdapter

    private var userId: String = ""
    private var chatId: String = ""
    private var matchUserId: String = ""
    private var matchUsername: String = ""
    private var matchProfileImageUrl: String? = null

    companion object {
        private const val CHAT_PARAM_CHAT_ID = "Chat id"
        private const val CHAT_PARAM_USER_ID = "User id"
        private const val CHAT_PARAM_MATCH_USER_ID = "Match user id"
        private const val CHAT_PARAM_MATCH_PROFILE_IMAGE_URL = "Profile Image url"
        private const val CHAT_PARAM_MATCH_USERNAME = "Profile username"

        fun newIntent(
            context: Context?,
            chatId: String?,
            userId: String?,
            matchUserId: String?,
            matchProfileImageUrl: String?,
            matchUsername: String?,
        ): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(CHAT_PARAM_CHAT_ID, chatId)
            intent.putExtra(CHAT_PARAM_USER_ID, userId)
            intent.putExtra(CHAT_PARAM_MATCH_USER_ID, matchUserId)
            intent.putExtra(CHAT_PARAM_MATCH_PROFILE_IMAGE_URL, matchProfileImageUrl)
            intent.putExtra(CHAT_PARAM_MATCH_USERNAME, matchUsername)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityChatBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // Get passed-in params
        chatId = intent.extras?.getString(CHAT_PARAM_CHAT_ID) ?: ""
        userId = intent.extras?.getString(CHAT_PARAM_USER_ID) ?: ""
        matchUserId = intent.extras?.getString(CHAT_PARAM_MATCH_USER_ID) ?: ""
        matchProfileImageUrl = intent.extras?.getString(CHAT_PARAM_MATCH_PROFILE_IMAGE_URL)
        matchUsername = intent.extras?.getString(CHAT_PARAM_MATCH_USERNAME) ?: ""
        if (chatId.isEmpty()
            || userId.isEmpty()
            || matchUserId.isEmpty()
        ) {
            Toast.makeText(this, "Chat room error", Toast.LENGTH_SHORT).show()
            finish()
        }

        bind.usernameTv.text = matchUsername
        bind.profileImageIv.loadUrl(matchProfileImageUrl)
        bind.profileImageIv.setOnClickListener {
            startActivity(UserInfoActivity.newIntent(this@ChatActivity, matchUserId))
        }

        chatDatabase = FirebaseDatabase.getInstance()
            .reference
            .child(DATA_CHATS_COLLECTION)

        messagesAdapter = MessagesAdapter(ArrayList(), userId)
        bind.messagesRv.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = messagesAdapter
        }

        // Add listener for new messages
        chatDatabase.child(chatId)
            .child(DATA_CHAT_MESSAGES)
            .orderByChild(DATA_CHAT_MESSAGES_TIME_LONG)
            .addChildEventListener(chatMessagesListener)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSend(v: View) {
        val message = Message(userId,
            bind.messageToSendEt.text.toString(),
            Calendar.getInstance().time.toString(),
            System.currentTimeMillis()
        )
        val key = chatDatabase.child(chatId)
            .child(DATA_CHAT_MESSAGES)
            .push().key

        if(!key.isNullOrEmpty()) {
            chatDatabase.child(chatId)
                .child(DATA_CHAT_MESSAGES)
                .child(key)
                .setValue(message)
        }

        // Clear the send message EditText
        bind.messageToSendEt.setText("", TextView.BufferType.EDITABLE)
    }

    private val chatMessagesListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildMoved(messageDoc: DataSnapshot, p1: String?) {}
        override fun onChildChanged(messageDoc: DataSnapshot, p1: String?) {}
        override fun onChildRemoved(messageDoc: DataSnapshot) {}

        override fun onChildAdded(messageDoc: DataSnapshot, p1: String?) {
            val message = messageDoc.getValue(Message::class.java)

            if (message != null) {
                messagesAdapter.addMessage(message)
                bind.messagesRv.post {
                    bind.messagesRv.smoothScrollToPosition(messagesAdapter.itemCount - 1)
                }
            }
        }

    }
}