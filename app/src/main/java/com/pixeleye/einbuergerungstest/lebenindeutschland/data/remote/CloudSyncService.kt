package com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Backs up the user's progress (Score, Streaks, Bookmarks).
     * Uses merge to avoid overwriting fields not included in the update.
     */
    suspend fun saveUserProgress(userId: String, progressData: Map<String, Any>) {
        try {
            firestore.collection("users")
                .document(userId)
                .set(progressData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves backed-up progress data for the user.
     */
    suspend fun getUserProgress(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            document.data
        } catch (e: Exception) {
            null
        }
    }
}
