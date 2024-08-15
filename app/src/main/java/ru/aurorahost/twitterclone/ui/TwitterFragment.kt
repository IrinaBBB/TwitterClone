package ru.aurorahost.twitterclone.ui

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ru.aurorahost.twitterclone.adapters.TweetListAdapter
import ru.aurorahost.twitterclone.listeners.HomeCallback
import ru.aurorahost.twitterclone.listeners.TwitterListenerImpl
import ru.aurorahost.twitterclone.util.User

abstract class TwitterFragment : Fragment() {
    protected var tweetsAdapter: TweetListAdapter? = null
    protected var currentUser: User? = null
    protected val firebaseReference = FirebaseDatabase.getInstance().reference
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid
    protected var listener: TwitterListenerImpl? = null
    protected var callback: HomeCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeCallback) {
            callback = context
        } else {
            throw RuntimeException("$context must implement HomeCallback")
        }
    }

    fun setUser(user: User?) {
        this.currentUser = user
        listener?.user = user
    }

    abstract fun updateList()

    override fun onResume() {
        super.onResume()
        updateList()
    }
}