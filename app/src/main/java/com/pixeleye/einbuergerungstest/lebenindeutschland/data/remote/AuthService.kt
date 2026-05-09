package com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthService {
    val userFlow: kotlinx.coroutines.flow.StateFlow<FirebaseUser?>
    suspend fun signInAnonymously(): FirebaseUser?
    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser?
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): FirebaseUser?
    suspend fun linkAnonymousWithEmail(email: String, password: String): FirebaseUser?
    suspend fun sendPasswordResetEmail(email: String): Boolean
    suspend fun updateDisplayName(name: String): Boolean
    suspend fun updatePhotoUri(uri: String): Boolean
    suspend fun updatePassword(newPassword: String): Boolean
    suspend fun sendEmailVerification(): Boolean
    fun signOut()
    fun getCurrentUser(): FirebaseUser?
}

@Singleton
class AuthServiceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthService {

    private val _userFlow = kotlinx.coroutines.flow.MutableStateFlow(firebaseAuth.currentUser)
    override val userFlow = _userFlow.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _userFlow.value = auth.currentUser
        }
    }

    override suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String): FirebaseUser? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            user?.updateProfile(com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            })?.await()
            user?.reload()?.await()
            firebaseAuth.currentUser
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun linkAnonymousWithEmail(email: String, password: String): FirebaseUser? {
        val user = firebaseAuth.currentUser ?: return null
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        return try {
            val result = user.linkWithCredential(credential).await()
            result.user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateDisplayName(name: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.updateProfile(com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }).await()
            user.reload().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePhotoUri(uri: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.updateProfile(com.google.firebase.auth.userProfileChangeRequest {
                photoUri = android.net.Uri.parse(uri)
            }).await()
            user.reload().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePassword(newPassword: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.updatePassword(newPassword).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun sendEmailVerification(): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.sendEmailVerification().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}




