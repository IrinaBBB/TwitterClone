package ru.aurorahost.twitterclone.listeners

import ru.aurorahost.twitterclone.util.Tweet

interface TweetListener {
    fun onLayoutClick(tweet: Tweet?)
    fun onLike(tweet: Tweet?)
    fun onRetweet(tweet: Tweet?)
}