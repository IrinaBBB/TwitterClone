package ru.aurorahost.twitterclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import ru.aurorahost.twitterclone.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onLogin(v: View) {

    }

    fun goToSignUp(v: View) {

    }
}