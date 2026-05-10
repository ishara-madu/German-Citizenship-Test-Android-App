package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthService
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class ActionSuccess(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authService.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAnonymous = MutableStateFlow(authService.getCurrentUser()?.isAnonymous ?: true)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    init {
        val user = authService.getCurrentUser()
        if (user != null) {
            _authState.value = AuthState.Success
            _isAnonymous.value = user.isAnonymous
        } else {
            signInAnonymously()
        }
    }

    private fun updateUserData(user: FirebaseUser?) {
        _currentUser.value = user
        _isAnonymous.value = user?.isAnonymous ?: true
        // Sync RevenueCat user with Firebase UID
        if (user != null && !user.isAnonymous) {
            subscriptionRepository.loginUser(user.uid)
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = authService.signInAnonymously()
            if (user != null) {
                updateUserData(user)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Anonymous sign in failed")
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = authService.signInWithEmailAndPassword(email, pass)
            if (user != null) {
                updateUserData(user)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Sign in failed. Please check your credentials.")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String, avatar: String? = null) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        val finalAvatar = avatar ?: "avatar_${(1..8).random()}"

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val currentUser = authService.getCurrentUser()
            val user = if (currentUser != null && currentUser.isAnonymous) {
                val linkedUser = authService.linkAnonymousWithEmail(email, password)
                if (linkedUser != null) {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                        photoUri = android.net.Uri.parse(finalAvatar)
                    }
                    linkedUser.updateProfile(profileUpdates).await()
                    linkedUser.reload().await()
                }
                linkedUser
            } else {
                val newUser = authService.signUpWithEmailAndPassword(email, password, name)
                if (newUser != null) {
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        photoUri = android.net.Uri.parse(finalAvatar)
                    }
                    newUser.updateProfile(profileUpdates).await()
                    newUser.reload().await()
                }
                newUser
            }

            if (user != null) {
                updateUserData(user)
                authService.sendEmailVerification() // Send confirmation email
                _authState.value = AuthState.Success
                _authState.value = AuthState.ActionSuccess("Account created! Please check your email for a confirmation link.")
            } else {
                _authState.value = AuthState.Error("Sign up failed. Email might be in use or connection error.")
            }

        }
    }



    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            signInAnonymously() // Re-sign in as guest
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authService.sendPasswordResetEmail(email)
            if (success) {
                _authState.value = AuthState.ActionSuccess("Password reset email sent!")
            } else {
                _authState.value = AuthState.Error("Failed to send reset email. Please check the email address.")
            }
        }
    }

    fun updateProfile(name: String, avatar: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            var success = authService.updateDisplayName(name)
            if (avatar != null) {
                val avatarSuccess = authService.updatePhotoUri(avatar)
                success = success && avatarSuccess
            }
            
            if (success) {
                updateUserData(authService.getCurrentUser())
                _authState.value = AuthState.ActionSuccess("Profile updated successfully!")
            } else {
                _authState.value = AuthState.Error("Failed to update profile.")
            }
        }
    }

    fun updatePassword(newPassword: String) {
        if (newPassword.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authService.updatePassword(newPassword)
            if (success) {
                _authState.value = AuthState.ActionSuccess("Password changed successfully!")
            } else {
                _authState.value = AuthState.Error("Failed to change password. You may need to re-login.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

