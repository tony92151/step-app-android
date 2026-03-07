package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity

interface AuthGateway {
    suspend fun signIn(activity: Activity): Result<SignedInUser>
    suspend fun signOut(activity: Activity): Result<Unit>
}

data class SignedInUser(
    val id: String,
    val displayName: String?,
    val email: String?,
    val profilePictureUri: String?,
)
