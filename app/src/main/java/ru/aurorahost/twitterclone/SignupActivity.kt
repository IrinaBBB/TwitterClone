package ru.aurorahost.twitterclone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import ru.aurorahost.twitterclone.databinding.ActivitySignupBinding
import ru.aurorahost.twitterclone.util.DATA_USERS
import ru.aurorahost.twitterclone.util.User

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    private lateinit var databaseReference: DatabaseReference
    private var firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this@SignupActivity))
            finish()
        }
        setTextOnChangeListener(binding.etEmail, binding.tilEmail)
        setTextOnChangeListener(binding.etPassword, binding.tilPassword)

        //binding.progressBar.setOnTouchListener { v, event ->  true}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTextOnChangeListener(binding.etEmail, binding.tilEmail)
        setTextOnChangeListener(binding.etPassword, binding.tilPassword)
    }

    fun onSignUp(v: View) {
        var proceed = true
        if (binding.etUsername.text.isNullOrEmpty()) {
            binding.etUsername.error = "Username is required"
            binding.tilUsername.isErrorEnabled = true
            proceed = false
        }

        if (binding.etEmail.text.isNullOrEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.tilEmail.isErrorEnabled = true
            proceed = false
        }

        if (binding.etPassword.text.isNullOrEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.tilPassword.isErrorEnabled = true
            proceed = false
        }

//        if (binding.etPassword.text?.trim() != binding.etConfirmPassword.text?.trim()) {
//            binding.etConfirmPassword.error = "Password and Confirm Password do not match"
//            binding.tilConfirmPassword.isErrorEnabled = true
//            proceed = false
//        }

        if (proceed) {
            binding.progressBar.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(binding.etEmail.text.toString().trim(), binding.etPassword.text.toString().trim())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Sign up error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } else {
                        databaseReference = FirebaseDatabase.getInstance().reference.child(DATA_USERS)
                        val name = binding.etUsername.text.toString().trim()
                        val email = binding.etEmail.text.toString().trim()
                        val password = binding.etPassword.text.toString().trim()

                        val user = User(email = email, username = name, imageUrl = "", followHashtags = arrayListOf(), followUsers = arrayListOf())
                        databaseReference.child(firebaseAuth.uid!!).setValue(user)
                    }
                    binding.progressBar.visibility = View.GONE
                }
                .addOnFailureListener {e ->
                    e.printStackTrace()
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }



    private fun setTextOnChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun goToLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}