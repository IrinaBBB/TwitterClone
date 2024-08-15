package ru.aurorahost.twitterclone.ui.home

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.aurorahost.twitterclone.util.DATA_TWEETS
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class HomeViewModel() : ViewModel() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseReference = FirebaseDatabase.getInstance().reference

    private val _tweets = MutableLiveData<ArrayList<Tweet>?>()
    val tweets: LiveData<ArrayList<Tweet>?> get() = _tweets
    val changeTrigger = MutableLiveData<Boolean>()

    init {
        changeTrigger.value = false
    }

    fun updateList(currentUser: User?, recyclerView: RecyclerView, progressBar: ProgressBar) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        firebaseReference.child(DATA_TWEETS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tweetsFromDb = ArrayList<Tweet>()
                    for (tweetSnapshot in dataSnapshot.children) {
                        val tweet = tweetSnapshot.getValue(Tweet::class.java) ?: continue
                        if (currentUser?.followHashtags?.any {
                                tweet.hashtags?.contains(it) == true
                            } == true ||
                            currentUser?.followUsers?.contains(tweet.userIds!![0]) == true) {
                            tweetsFromDb.add(tweet)
                        }

                    }
                    _tweets.value = tweetsFromDb
                    // updateAdapter(tweets)
                    recyclerView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("DatabaseError", "Error fetching tweets: ", databaseError.toException())
                    recyclerView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            })
    }

    fun logOut() {
        firebaseAuth.signOut()
    }
}