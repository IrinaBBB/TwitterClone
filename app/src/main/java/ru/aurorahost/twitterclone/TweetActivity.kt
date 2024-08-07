package ru.aurorahost.twitterclone

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ru.aurorahost.twitterclone.databinding.ActivityTweetBinding
import ru.aurorahost.twitterclone.util.DATA_TWEETS
import ru.aurorahost.twitterclone.util.DATA_TWEETS_IMAGES
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.User

class TweetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTweetBinding

    /** Auth */
    private var userId: String? = null
    private var currentUser: User? = null

    /** DB */
    private lateinit var tweetsReference: DatabaseReference
    private lateinit var usersReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    /** Photo Uri */
    private var localFileUri: Uri? = null
    private var serverFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tweetsReference = FirebaseDatabase.getInstance().reference.child(DATA_TWEETS)
        usersReference = FirebaseDatabase.getInstance().reference.child(DATA_USERS)
        storageReference = FirebaseStorage.getInstance().reference

        if (intent.hasExtra(PARAM_USER_ID)) {
            userId = intent.getStringExtra(PARAM_USER_ID)
            getUserById(userId!!)
        } else {
            Toast.makeText(this, "Error creating a tweet", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.ivImage.setOnClickListener {
            pickImage(it)
        }

        binding.fabSaveTweet.setOnClickListener {
            postTweet(it)
        }
    }

    fun postTweet(v: View) {
        binding.progressBar.visibility = View.VISIBLE
        binding.group.visibility = View.INVISIBLE

        val text = binding.etTweetText.text.toString().trim()
        val hashtags = getHashTags(text)

        val tweetId = tweetsReference.push().key
        if (tweetId == null) {
            Toast.makeText(this, "Failed to get unique ID", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            binding.group.visibility = View.VISIBLE
            return
        }

        if (localFileUri != null) {
            val imageRef = storageReference.child("${DATA_TWEETS_IMAGES}/${tweetId}.jpg")
            imageRef.putFile(localFileUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    serverFileUri = uri
                    saveTweetToDatabase(tweetId, text, hashtags, serverFileUri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
            }
        } else {
            saveTweetToDatabase(tweetId, text, hashtags, "")
        }
    }

    private fun saveTweetToDatabase(tweetId: String, text: String, hashtags: ArrayList<String>, imageUrl: String) {
        val tweet = Tweet(
            tweetId = tweetId, text = text, hashtags = hashtags,
            imageUrl = imageUrl, userIds = arrayListOf(userId!!),
            username = currentUser?.username, timeStamp = System.currentTimeMillis()
        )

        tweetsReference.child(tweetId).setValue(tweet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Tweet posted successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after posting
            } else {
                Toast.makeText(
                    this,
                    "Failed to post tweet: ${task.exception?.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.progressBar.visibility = View.GONE
            binding.group.visibility = View.VISIBLE
        }
    }

    private fun getUserById(userId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.group.visibility = View.INVISIBLE

        usersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        // Update UI with user data
                        currentUser = it
                    }
                } else {
                    Toast.makeText(this@TweetActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@TweetActivity, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
            }
        })
    }

    private fun getHashTags(source: String): ArrayList<String> {
        val hashtags = arrayListOf<String>()
        var text = source

        while (text.contains("#")) {
            var hashtag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)

            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if (firstSpace == -1 && firstHash == -1) {
                hashtag = text.substring(0)
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else if (firstHash != -1) {
                hashtag = text.substring(0, firstHash)
                text = text.substring(firstHash)
            } else {
                hashtag = text.substring(0)
            }

            if (hashtag.isNotEmpty()) {
                hashtags.add(hashtag)
            }
        }
        return hashtags
    }

    fun pickImage(v: View) {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> imagePickerLauncher.launch(intent) }
    }

    companion object {
        private const val PARAM_USER_ID = "UserId"
        fun newIntent(context: Context, userId: String?): Intent {
            val intent = Intent(context, TweetActivity::class.java)
            intent.putExtra(PARAM_USER_ID, userId)
            return intent
        }
    }

    private val imagePickerLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            localFileUri = result.data?.data
            binding.ivImage.setImageURI(localFileUri)
        }
    }
}
