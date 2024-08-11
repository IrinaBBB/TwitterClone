package ru.aurorahost.twitterclone.util

data class User(
    val email: String? = "",
    val username: String? = "",
    val imageUrl: String? = "",
    var followHashtags: ArrayList<String>? = arrayListOf(),
    val followUsers: ArrayList<String>? = arrayListOf()
)

data class Tweet(
    val tweetId: String? = "",
    val userIds: ArrayList<String>? = arrayListOf(),
    val username: String? = "",
    val text: String? = "",
    val imageUrl: String? = "",
    val timeStamp: Long? = 0,
    val hashtags: ArrayList<String>? = arrayListOf(),
    val likes: ArrayList<String>? = arrayListOf()
)