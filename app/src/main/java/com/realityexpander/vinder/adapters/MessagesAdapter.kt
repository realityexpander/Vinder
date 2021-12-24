package com.realityexpander.vinder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.vinder.R
import com.realityexpander.vinder.models.Message

const val MESSAGE_CURRENT_USER = 1
const val MESSAGE_MATCHED_USER = 2

class MessagesAdapter(private var messages: ArrayList<Message>, val userId: String) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {


    fun addMessage(message: Message) {
        messages.add(message)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, itemViewType: Int): MessageViewHolder {
        when(itemViewType) {
            MESSAGE_CURRENT_USER -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_current_user_message, parent, false)
                )
            }
            MESSAGE_MATCHED_USER -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_matched_user_message, parent, false)
                )
            }
            else -> {
                return MessageViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_current_user_message, parent, false)
                )
            }
        }
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemViewType(position: Int): Int {
        if(messages[position].sentBy.equals(userId)) {
            return MESSAGE_CURRENT_USER
        } else {
            return MESSAGE_MATCHED_USER
        }
    }

    class MessageViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(message: Message) {
            view.findViewById<TextView>(R.id.messageTV).text = message.message
        }
    }
}