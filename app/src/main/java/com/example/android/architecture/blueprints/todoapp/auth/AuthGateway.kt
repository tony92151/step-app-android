package com.example.android.architecture.blueprints.todoapp.auth

interface AuthGateway {
    suspend fun signIn(): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
