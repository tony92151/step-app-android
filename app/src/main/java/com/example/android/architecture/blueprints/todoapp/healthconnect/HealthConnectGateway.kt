package com.example.android.architecture.blueprints.todoapp.healthconnect

import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft

interface HealthConnectGateway {
    suspend fun syncRunSession(runSessionDraft: RunSessionDraft): Result<Unit>
}
