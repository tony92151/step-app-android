package com.example.android.architecture.blueprints.todoapp.healthconnect

import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft

interface HealthConnectGateway {
    val requiredPermissions: Set<String>

    fun getAvailability(): HealthConnectAvailability

    suspend fun getGrantedPermissions(): Set<String>

    suspend fun syncRunSession(runSessionDraft: RunSessionDraft): Result<Unit>
}
