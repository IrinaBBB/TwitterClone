package ru.aurorahost.twitterclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import ru.aurorahost.twitterclone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var firebaseAuth = FirebaseAuth.getInstance()

    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this@LoginActivity))
            finish()
        }
        setTextOnChangeListener(binding.etEmail, binding.tilEmail)
        setTextOnChangeListener(binding.etPassword, binding.tilPassword)
        
        binding.progressBar.setOnTouchListener { v, event ->  true}
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }


    fun onLogin(v: View) {
        var proceed = true
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

        if (proceed) {
            binding.progressBar.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(binding.etEmail.text.toString().trim(), binding.etPassword.text.toString().trim())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Login error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {e ->
                    e.printStackTrace()
                    binding.progressBar.visibility = View.GONE
                }
        }
    }

    fun goToSignUp(v: View) {

    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }


}