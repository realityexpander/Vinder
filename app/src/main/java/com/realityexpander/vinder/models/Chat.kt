package com.realityexpander.vinder.models

data class Chat(
    val chatId: String? = "",
    val userId: String? = "",
    val matchedUserId: String? = "",
    val username: String? = "",  // for the match User
    val profileImageUrl: String? = "" // for the matched User
)

data class Message(
    val sentBy: String? = null,
    val message: String? = null,
    val time: String? = null,
    val timeMsLong: Long = 0L
)