package ru.aurorahost.twitterclone.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.User

class MainViewModel : ViewModel() {

    private val firebaseReference = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun getCurrentUser() {
        if (userId != null) {
            firebaseReference.child(DATA_USERS).child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userFromDb = dataSnapshot.getValue(User::class.java)
                            _user.value = userFromDb
                        } else {
                            _user.value = null
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("getCurrentUser", "Error fetching user", databaseError.toException())
                    }
                })
        } else {
            _user.value = null
        }
    }
}
