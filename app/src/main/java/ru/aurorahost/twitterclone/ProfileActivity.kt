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
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ru.aurorahost.twitterclone.databinding.ActivityLoginBinding
import ru.aurorahost.twitterclone.databinding.ActivityProfileBinding
import ru.aurorahost.twitterclone.databinding.ActivitySignupBinding
import ru.aurorahost.twitterclone.util.DATA_IMAGE_URL
import ru.aurorahost.twitterclone.util.DATA_PROFILE_IMAGES
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.DATA_USER_EMAIL
import ru.aurorahost.twitterclone.util.DATA_USER_USERNAME
import ru.aurorahost.twitterclone.util.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    /** Auth */
    private lateinit var firebaseAuth: FirebaseAuth
    private var userId: String? = null

    /** DB */
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    /** Photo Uri */
    private var localFileUri: Uri? = null
    private var serverFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid

        storageReference = FirebaseStorage.getInstance().reference.child(DATA_PROFILE_IMAGES)

        if (userId == null) {
            finish()
        }
        populateInfo(userId!!)
        Glide.with(this)
            .load(serverFileUri)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(binding.ivProfile)
    }

    private fun populateInfo(userId: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.group.visibility = View.INVISIBLE

        databaseReference = FirebaseDatabase.getInstance().reference.child(DATA_USERS).child(userId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        binding.etUsername.setText(it.username)
                        binding.etEmail.setText(it.email)
                        Glide.with(this@ProfileActivity)
                            .load(it.imageUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(binding.ivProfile)
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
            }
        })

    }

    fun onApply(v: View) {
        binding.progressBar.visibility = View.VISIBLE
        binding.group.visibility = View.INVISIBLE

        if (localFileUri != null) {
            val filePath = storageReference.child("$userId.jpg")
            filePath.putFile(localFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        serverFileUri = uri
                        updateProfileWithImageUrl()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.group.visibility = View.VISIBLE
                }
        } else {
            updateProfile()
        }
    }

    private fun updateProfileWithImageUrl() {
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = binding.etUsername.text.toString().trim()
        map[DATA_USER_EMAIL] = binding.etEmail.text.toString().trim()
        map[DATA_IMAGE_URL] = serverFileUri.toString()

        databaseReference = FirebaseDatabase.getInstance().reference.child(DATA_USERS).child(userId!!)
        databaseReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Profile update failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            binding.progressBar.visibility = View.GONE
            binding.group.visibility = View.VISIBLE
        }
    }

    private fun updateProfile() {
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = binding.etUsername.text.toString().trim()
        map[DATA_USER_EMAIL] = binding.etEmail.text.toString().trim()

        databaseReference = FirebaseDatabase.getInstance().reference.child(DATA_USERS).child(userId!!)
        databaseReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                Glide.with(this)
                    .load(serverFileUri)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(binding.ivProfile)
            } else {
                Toast.makeText(this, "Profile update failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            binding.progressBar.visibility = View.GONE
            binding.group.visibility = View.VISIBLE
        }
    }

    fun pickImage(v: View) {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> imagePickerLauncher.launch(intent) }
    }

    fun onLogout(v: View) {
        firebaseAuth.signOut()
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }

    private val imagePickerLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            localFileUri = result.data?.data
            binding.ivProfile.setImageURI(localFileUri)
        }
    }
}