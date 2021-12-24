package com.realityexpander.vinder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.vinder.R
import com.realityexpander.vinder.activities.ChatActivity
import com.realityexpander.vinder.models.Chat
import com.realityexpander.vinder.utils.loadUrl

class ChatsAdapter(private var chats: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    fun addElement(chat: Chat) {
        chats.add(chat)
        notifyDataSetChanged()
    }

    fun clear() {
        chats.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) =
        ChatsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        )

    override fun getItemCount() = chats.size

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    class ChatsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private var layout = view.findViewById<View>(R.id.chatLayout)
        private var profileImageIv = view.findViewById<ImageView>(R.id.chatProfileImageIv)
        private var usernameTv = view.findViewById<TextView>(R.id.chatUsernameTv)

        fun bind(chat: Chat) {
            usernameTv.text = chat.username
            if (profileImageIv != null) {
                profileImageIv.loadUrl(chat.profileImageUrl)
            }

            layout.setOnClickListener {
                view.context.startActivity(
                    ChatActivity.newIntent(
                        view.context,
                        chat.chatId,
                        chat.userId,
                        chat.matchedUserId,
                        chat.profileImageUrl
                    )
                )
            }
        }

    }
}