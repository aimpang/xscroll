package com.xscroll.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xscroll.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection = firestore.collection("users")

    suspend fun signInAnonymously(): User {
        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw IllegalStateException("Anonymous sign-in failed")

        val docRef = usersCollection.document(uid)
        val doc = docRef.get().await()

        return if (doc.exists()) {
            doc.toObject(User::class.java)!!.copy(uid = uid)
        } else {
            val newUser = User(uid = uid)
            docRef.set(newUser).await()
            newUser
        }
    }

    fun getCurrentUid(): String? = auth.currentUser?.uid

    suspend fun getUser(uid: String): User? {
        val doc = usersCollection.document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(uid = doc.id)
    }

    suspend fun deductToken(uid: String): Boolean {
        val docRef = usersCollection.document(uid)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentTokens = snapshot.getLong("tokens")?.toInt() ?: 0
            if (currentTokens <= 0) return@runTransaction false
            transaction.update(docRef, "tokens", currentTokens - 1)
            true
        }.await()
    }
}
