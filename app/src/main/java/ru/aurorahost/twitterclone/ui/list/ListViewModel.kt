package ru.aurorahost.twitterclone.ui.list

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
import ru.aurorahost.twitterclone.util.DATA_TWEETS_USER_IDS
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class ListViewModel : ViewModel() {

    private val firebaseReference = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid


    private val _tweets = MutableLiveData<ArrayList<Tweet>?>()
    val tweets: LiveData<ArrayList<Tweet>?> get() = _tweets


    fun updateList(currentUser: User?, recyclerView: RecyclerView, progressBar: ProgressBar) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        val tweetsFromDb = mutableListOf<Tweet>()

        firebaseReference.child(DATA_TWEETS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val tweet = snapshot.getValue(Tweet::class.java)
                        tweet?.let { tweetsFromDb.add(it) }
                    }

                    val sortedList = tweetsFromDb.sortedByDescending { it.timeStamp }
                    _tweets.value = ArrayList(sortedList)
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


}