package ru.aurorahost.twitterclone

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.aurorahost.twitterclone.databinding.ActivityHomeBinding
import ru.aurorahost.twitterclone.databinding.ActivityTweetBinding

class TweetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTweetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, TweetActivity::class.java)
    }
}