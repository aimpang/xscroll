package com.xscroll.data.model

import com.google.firebase.Timestamp

data class DanmakuMessage(
    val id: String = "",
    val text: String = "",
    val color: String = "#FFFFFF",
    val timestampMs: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val userId: String = "",
)
