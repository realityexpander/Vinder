package com.realityexpander.vinder.utils

// ------- USERS COLLECTION ---------
const val DATA_USERS_COLLECTION = "Users"
const val DATA_USER_USERNAME = "username"
const val DATA_USER_EMAIL = "email"
const val DATA_USER_AGE = "age"
const val DATA_USER_GENDER = "gender"
const val DATA_USER_GENDER_PREFERENCE = "preferredGender"
const val PREFERENCE_GENDER_MALE = "male"
const val PREFERENCE_GENDER_FEMALE = "female"
const val DATA_USER_PROFILE_IMAGE_URL = "profileImageUrl"
const val DATA_USER_SWIPED_LEFT_USER_IDS = "swipeLeftUserIds"
const val DATA_USER_SWIPED_RIGHT_USER_IDS = "swipeRightUserIds"
const val DATA_USER_MATCH_USER_ID_TO_CHAT_IDS = "matchUserIdToChatIds"

//Users
//+ CNjTUvInJmWn3gQNZzjaTvpX3Il2 (userId)
//    age: "34" (String)
//    email: "ca@test.com" (String)
//    gender: "male" (String)
//  + matchUserIdToChatIds
//      qiyk4zEtGzXYqfCO8uHIKmCwMey1 (userId) -> Mrk6lmbACMhcse2Inqx (chatId)
//    preferredGender: "female" (String)
//    profileImageUrl: http://...
//  + swipeLeftUserIds
//      gLQk7GXzBRO8Jozi16fafBIle7l2 (userId): true
//  + swipeRightUserIds
//      gLQk7GXzBRO8Jozi16fafBIle7l2 (userId): true
//    uid: CNjTUvInJmWn3gQNZzjaTvpX3Il2 (String)
//    username: String

// ------- CHATS COLLECTION ---------
const val DATA_CHATS_COLLECTION = "Chats"
  // sub-collection: messages
const val DATA_CHAT_MESSAGES = "Messages"
const val DATA_CHAT_MESSAGES_TIME_LONG = "timeMsLong"

//Chats
//+ Mrk6lmbACMhcse2Inqx (chatId)
//  + CNjTUvInJmWn3gQNZzjaTvpX3Il2 (userId)
//       profileImageUrl: http://...
//       username: Janet (String)
//
//  + qiyk4zEtGzXYqfCO8uHIKmCwMey1
//     profileImageUrl: http://...
//     username: Chris (String)
//
//  - Messages
//    + MrkBmf3niLbOb1xuZSG (userId)
//        message: String
//        sentBy: qiyk4zEtGzXYqfCO8uHIKmCwMey1 (userId)
//        time: Sun Dec 25 2021 (String)
//        timeMsLong: 1640438382 (String)


// ------- IMAGE STORAGE --------
const val DATA_IMAGE_STORAGE_PROFILE_IMAGES = "profileImage"