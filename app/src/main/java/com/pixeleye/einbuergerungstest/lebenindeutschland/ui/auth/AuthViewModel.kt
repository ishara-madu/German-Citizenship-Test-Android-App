package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.pixeleye.einbuergerungstest.lebenindeutschland.R
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthService
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.SubscriptionRepository
import com.pixeleye.einbuergerungstest.lebenindeutschland.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
            val msg = context.getString(R.string.err_fill_all_fields)
            SnackbarManager.showWarning(messageResId = R.string.err_fill_all_fields)
            _authState.value = AuthState.Error(msg)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = authService.signInWithEmailAndPassword(email, pass)
            if (user != null) {
                updateUserData(user)
                SnackbarManager.showSuccess(messageResId = R.string.success_signed_in)
                _authState.value = AuthState.Success
            } else {
                val msg = context.getString(R.string.err_sign_in_failed)
                SnackbarManager.showError(messageResId = R.string.err_sign_in_failed)
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String, avatar: String? = null) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            val msg = context.getString(R.string.err_fill_all_fields)
            SnackbarManager.showWarning(messageResId = R.string.err_fill_all_fields)
            _authState.value = AuthState.Error(msg)
            return
        }
        if (password.length < 6) {
            val msg = context.getString(R.string.err_password_length)
            SnackbarManager.showWarning(messageResId = R.string.err_password_length)
            _authState.value = AuthState.Error(msg)
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
                authService.sendEmailVerification() // Send confirmation email
                authService.signOut()
                updateUserData(null)
                val msg = context.getString(R.string.success_account_created)
                SnackbarManager.showSuccess(messageResId = R.string.success_account_created)
                _authState.value = AuthState.ActionSuccess(msg)
            } else {
                val msg = context.getString(R.string.err_sign_up_failed)
                SnackbarManager.showError(messageResId = R.string.err_sign_up_failed)
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            SnackbarManager.showInfo(messageResId = R.string.success_signed_out)
            signInAnonymously() // Re-sign in as guest
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            val msg = context.getString(R.string.err_enter_email)
            SnackbarManager.showWarning(messageResId = R.string.err_enter_email)
            _authState.value = AuthState.Error(msg)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authService.sendPasswordResetEmail(email)
            if (success) {
                val msg = context.getString(R.string.success_reset_email_sent)
                SnackbarManager.showSuccess(messageResId = R.string.success_reset_email_sent)
                _authState.value = AuthState.ActionSuccess(msg)
            } else {
                val msg = context.getString(R.string.err_reset_email_failed)
                SnackbarManager.showError(messageResId = R.string.err_reset_email_failed)
                _authState.value = AuthState.Error(msg)
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
                val msg = context.getString(R.string.success_profile_updated)
                SnackbarManager.showSuccess(messageResId = R.string.success_profile_updated)
                _authState.value = AuthState.ActionSuccess(msg)
            } else {
                val msg = context.getString(R.string.err_profile_update_failed)
                SnackbarManager.showError(messageResId = R.string.err_profile_update_failed)
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun updatePassword(newPassword: String) {
        if (newPassword.length < 6) {
            val msg = context.getString(R.string.err_password_length)
            SnackbarManager.showWarning(messageResId = R.string.err_password_length)
            _authState.value = AuthState.Error(msg)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val success = authService.updatePassword(newPassword)
            if (success) {
                val msg = context.getString(R.string.success_password_changed)
                SnackbarManager.showSuccess(messageResId = R.string.success_password_changed)
                _authState.value = AuthState.ActionSuccess(msg)
            } else {
                val msg = context.getString(R.string.err_password_change_failed)
                SnackbarManager.showError(messageResId = R.string.err_password_change_failed)
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

