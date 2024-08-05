package ru.aurorahost.twitterclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import ru.aurorahost.twitterclone.databinding.ActivityHomeBinding
import ru.aurorahost.twitterclone.databinding.ActivityLoginBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var firebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onLogout(v: View) {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this@HomeActivity))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }
}