package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authGateway: AuthGateway,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = null,
                )
            }
            val result = authGateway.signIn(activity)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { user ->
                        currentState.copy(
                            isLoading = false,
                            user = user,
                            message = "Sign in successful",
                        )
                    },
                    onFailure = { error ->
                        val safeMessage = if (error.message.isNullOrBlank()) {
                            "Sign in canceled or failed"
                        } else {
                            error.message
                        }
                        currentState.copy(
                            isLoading = false,
                            message = safeMessage,
                        )
                    }
                )
            }
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authGateway.signOut(activity)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        currentState.copy(
                            isLoading = false,
                            user = null,
                            message = "Signed out",
                        )
                    },
                    onFailure = { error ->
                        currentState.copy(
                            isLoading = false,
                            message = error.message ?: "Sign out failed",
                        )
                    }
                )
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: SignedInUser? = null,
    val message: String? = null,
)
