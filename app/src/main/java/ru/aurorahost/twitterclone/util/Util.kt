package ru.aurorahost.twitterclone.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getDate(s: Long?): String {
    return s?.let {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = Date(it)
        dateFormat.format(date)
    } ?: "Invalid Date"
}