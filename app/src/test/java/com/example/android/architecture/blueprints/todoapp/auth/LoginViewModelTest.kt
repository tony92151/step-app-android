package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun signIn_success_updatesSignedInUser() = runTest {
        val viewModel = LoginViewModel(
            authGateway = object : AuthGateway {
                override suspend fun signIn(activity: Activity): Result<SignedInUser> {
                    return Result.success(
                        SignedInUser(
                            id = "user-id",
                            displayName = "Demo User",
                            email = "demo@example.com",
                            profilePictureUri = null,
                        )
                    )
                }

                override suspend fun signOut(activity: Activity): Result<Unit> = Result.success(Unit)
            }
        )

        viewModel.signIn(Activity())
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.user?.id).isEqualTo("user-id")
        assertThat(viewModel.uiState.value.message).isEqualTo("Sign in successful")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun signIn_error_keepsUserSignedOut() = runTest {
        val viewModel = LoginViewModel(
            authGateway = object : AuthGateway {
                override suspend fun signIn(activity: Activity): Result<SignedInUser> {
                    return Result.failure(IllegalStateException("sign in failed"))
                }

                override suspend fun signOut(activity: Activity): Result<Unit> = Result.success(Unit)
            }
        )

        viewModel.signIn(Activity())
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.user).isNull()
        assertThat(viewModel.uiState.value.message).isEqualTo("sign in failed")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
