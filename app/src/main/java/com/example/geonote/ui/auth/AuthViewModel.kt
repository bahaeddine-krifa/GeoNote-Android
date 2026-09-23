package com.example.geonote.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geonote.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { repository.signInWithEmail(email.trim(), password) }
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Erreur de connexion") }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            runCatching { repository.signUpWithEmail(email.trim(), password) }
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Erreur d'inscription") }
        }
    }

    fun loginWithGoogle(idToken: String?) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            if (idToken == null) {
                _state.value = AuthState.Error("Token Google manquant")
                return@launch
            }
            runCatching {
                repository.signInWithGoogle(idToken)
            }.onSuccess {
                _state.value = AuthState.Success
            }.onFailure { e ->
                Log.e("AuthViewModel", "Google auth error", e)
                _state.value = AuthState.Error("Échec Google: ${e.message}")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}