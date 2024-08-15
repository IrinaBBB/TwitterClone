package ru.aurorahost.twitterclone.listeners

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.aurorahost.twitterclone.util.DATA_TWEETS
import ru.aurorahost.twitterclone.util.DATA_TWEETS_LIKES
import ru.aurorahost.twitterclone.util.DATA_TWEETS_USER_IDS
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.DATA_USERS_FOLLOW
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class TwitterListenerImpl(
    val tweetList: RecyclerView,
    var user: User?,
) : TweetListener {

    private val firebaseDb = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onLayoutClick(tweet: Tweet?) {
        tweet?.let {
            val owner = tweet.userIds?.get(0)
            if (owner != userId) {
                if (user?.followUsers?.contains(owner) == true) {
                    AlertDialog.Builder(tweetList.context)
                        .setTitle("Unfollow ${tweet.username}?")
                        .setPositiveButton("yes") { _, _ ->
                            tweetList.isClickable = false
                            var followedUsers = user?.followUsers
                            if (followedUsers == null) {
                                followedUsers = arrayListOf()
                            }
                            followedUsers.remove(owner)
                            firebaseDb.child(DATA_USERS).child(userId!!).child(DATA_USERS_FOLLOW)
                                .setValue(followedUsers)
                                .addOnSuccessListener {
                                    tweetList.isClickable = true
                                }
                                .addOnFailureListener {
                                    tweetList.isClickable = true
                                }
                        }
                        .setNegativeButton("cancel") { _, _ -> }
                        .show()
                } else {
                    AlertDialog.Builder(tweetList.context)
                        .setTitle("Follow ${tweet.username}?")
                        .setPositiveButton("yes") { _, _ ->
                            tweetList.isClickable = false
                            var followedUsers = user?.followUsers
                            if (followedUsers == null) {
                                followedUsers = arrayListOf()
                            }
                            owner?.let {
                                followedUsers?.add(owner)
                                firebaseDb.child(DATA_USERS).child(userId!!)
                                    .child(DATA_USERS_FOLLOW).setValue(followedUsers)
                                    .addOnSuccessListener {
                                        tweetList.isClickable = true
                                    }
                                    .addOnFailureListener {
                                        tweetList.isClickable = true
                                    }
                            }
                        }
                        .setNegativeButton("cancel") { _, _ -> }
                        .show()
                }
            }
        }
    }

    override fun onLike(tweet: Tweet?) {
        tweet?.let {
            tweetList.isClickable = false
            val likes = tweet.likes
            if (tweet.likes?.contains(userId) == true) {
                likes?.remove(userId)
            } else {
                likes?.add(userId!!)
            }

            val tweetRef = firebaseDb.child(DATA_TWEETS).child(tweet.tweetId!!)
            tweetRef.child(DATA_TWEETS_LIKES).setValue(likes)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        tweetList.isClickable = true
                    } else {
                        Toast.makeText(tweetList.context, "Error", Toast.LENGTH_SHORT).show()
                        tweetList.isClickable = true
                    }
                }
        }
    }

    override fun onRetweet(tweet: Tweet?) {
        tweet?.let {
            tweetList.isClickable = false
            val retweets = tweet.userIds
            if (retweets?.contains(userId) == true) {
                retweets.remove(userId)
            } else {
                retweets?.add(userId!!)
            }
            val tweetRef = firebaseDb.child(DATA_TWEETS).child(tweet.tweetId!!)
            tweetRef.child(DATA_TWEETS_USER_IDS).setValue(retweets)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        tweetList.isClickable = true
                    } else {
                        Toast.makeText(tweetList.context, "Error", Toast.LENGTH_SHORT).show()
                        tweetList.isClickable = true
                    }
                }
        }
    }
}