package com.xscroll.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val tokens: Int = 10,
    val isSubscriber: Boolean = false,
    val subscribedUntil: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
)
