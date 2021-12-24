package com.realityexpander.vinder.models

data class Chat(
    val chatId: String? = "",
    val userId: String? = "",
    val matchedUserId: String? = "",
    val username: String? = "",
    val profileImageUrl: String? = ""
)

data class Message(
    val sentBy: String? = null,
    val message: String? = null,
    val time: String? = null
)