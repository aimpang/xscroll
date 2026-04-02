package com.xscroll.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xscroll.data.model.DanmakuMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DanmakuRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observeDanmaku(videoId: String): Flow<List<DanmakuMessage>> = callbackFlow {
        val ref = firestore.collection("videos").document(videoId)
            .collection("danmaku")
            .orderBy("timestampMs", Query.Direction.ASCENDING)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(DanmakuMessage::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(messages)
        }

        awaitClose { listener.remove() }
    }

    suspend fun sendDanmaku(videoId: String, message: DanmakuMessage) {
        firestore.collection("videos").document(videoId)
            .collection("danmaku")
            .add(message)
            .await()
    }
}
