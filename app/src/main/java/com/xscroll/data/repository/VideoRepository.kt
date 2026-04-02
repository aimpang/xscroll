package com.xscroll.data.repository

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.xscroll.data.model.Video
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    private val videosCollection = firestore.collection("videos")

    suspend fun getVideos(
        limit: Long = 10,
        after: DocumentSnapshot? = null,
    ): Pair<List<Video>, DocumentSnapshot?> {
        var query = videosCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (after != null) {
            query = query.startAfter(after)
        }

        val snapshot = query.get().await()
        val videos = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Video::class.java)?.copy(id = doc.id)
        }
        val lastDoc = snapshot.documents.lastOrNull()
        return Pair(videos, lastDoc)
    }

    suspend fun getDownloadUrl(storagePath: String): Uri {
        return storage.reference.child(storagePath).downloadUrl.await()
    }

    suspend fun uploadVideo(uid: String, videoUri: Uri): Video {
        val videoId = UUID.randomUUID().toString()
        val storagePath = "videos/$videoId.mp4"

        storage.reference.child(storagePath).putFile(videoUri).await()

        val video = Video(
            id = videoId,
            storageUrl = storagePath,
            uploadedBy = uid,
        )

        videosCollection.document(videoId).set(video).await()
        return video
    }
}
