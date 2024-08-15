package ru.aurorahost.twitterclone.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.aurorahost.twitterclone.R
import ru.aurorahost.twitterclone.listeners.TweetListener
import ru.aurorahost.twitterclone.util.Tweet
import ru.aurorahost.twitterclone.util.getDate

class TweetListAdapter(val userId: String, private val tweets: ArrayList<Tweet>) : RecyclerView.Adapter<TweetListAdapter.TweetViewHolder>() {

    private var listener: TweetListener? = null

    fun setListener(listener: TweetListener?) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTweets(newTweets: List<Tweet>) {
        tweets.clear()
        tweets.addAll(newTweets)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tweet, parent, false)
        return TweetViewHolder(view)
    }

    override fun getItemCount() = tweets.size

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val tweet = tweets[position]
        holder.usernameTextView.text = tweet.username
        holder.tweetTextView.text = tweet.text
        holder.dateTextView.text =  getDate(tweet.timeStamp)
        holder.likeCountTextView.text = tweet.likes?.size.toString()
        holder.retweetCountTextView.text = tweet.userIds?.size?.minus(1).toString()

        // Load image using Glide or any other image loading library
        if (tweet.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(tweet.imageUrl)
                .into(holder.tweetImageView)
            holder.tweetImageView.visibility = View.VISIBLE
        } else {
            holder.tweetImageView.visibility = View.GONE
        }

        holder.tweetLayout.setOnClickListener {
            listener?.onLayoutClick(tweet)
        }
        holder.likeImageView.setOnClickListener {
            listener?.onLike(tweet)
        }

        holder.retweetImageView.setOnClickListener {
            listener?.onRetweet(tweet)
        }

        if (tweet.likes?.contains(userId) == true) {
            holder.likeImageView.setImageDrawable(ContextCompat.getDrawable(holder.likeImageView.context, R.drawable.like))
        } else {
            holder.likeImageView.setImageDrawable(ContextCompat.getDrawable(holder.likeImageView.context, R.drawable.like_inactive))
        }

        if (tweet.userIds?.get(0).equals(userId)) {
            holder.retweetImageView.setImageDrawable(ContextCompat.getDrawable(holder.retweetImageView.context, R.drawable.original))
        } else if (tweet.userIds?.contains(userId) == true) {
            holder.retweetImageView.setImageDrawable(ContextCompat.getDrawable(holder.retweetImageView.context, R.drawable.retweet))
        } else {
            holder.retweetImageView.setImageDrawable(ContextCompat.getDrawable(holder.retweetImageView.context, R.drawable.retweet_inactive))
        }
    }

    class TweetViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tweetLayout: ConstraintLayout = v.findViewById(R.id.tweetLayout)
        val usernameTextView: TextView = v.findViewById(R.id.tweetUsername)
        val tweetTextView: TextView = v.findViewById(R.id.tweetText)
        val tweetImageView: ImageView = v.findViewById(R.id.tweetImage)
        val dateTextView: TextView = v.findViewById(R.id.tweetDate)
        val likeImageView: ImageView = v.findViewById(R.id.tweetLike)
        val likeCountTextView: TextView = v.findViewById(R.id.tweetLikeCount)
        val retweetImageView: ImageView = v.findViewById(R.id.tweetRetweet)
        val retweetCountTextView: TextView = v.findViewById(R.id.tweetRetweetCount)
    }
}