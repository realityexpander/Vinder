package com.realityexpander.vinder.models

data class User(
    val uid: String? = "",
    val username: String? = "",
    val age: String? = "",
    val email: String? = "",
    val gender: String? = "",
    val preferredGender: String? = "",
    val profileImageUrl: String? = "",

    val matchUserIdToChatIds: HashMap<String, String> = HashMap(),
    val swipeLeftUserIds: HashMap<String, Boolean> = HashMap(),
    val swipeRightUserIds: HashMap<String, Boolean> = HashMap(),
)