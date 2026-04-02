package com.xscroll.data.model

import com.google.firebase.Timestamp

data class Video(
    val id: String = "",
    val storageUrl: String = "",
    val uploadedBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val reportCount: Int = 0,
    val danmakuCount: Int = 0,
)
