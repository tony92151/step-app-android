package com.example.android.architecture.blueprints.todoapp.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseRoute
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidHealthConnectGateway @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthConnectGateway {

    override val requiredPermissions: Set<String> = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseRoute::class),
    )

    override fun getAvailability(): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectAvailability.UPDATE_REQUIRED
            }
            HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
    }

    override suspend fun getGrantedPermissions(): Set<String> {
        return if (getAvailability() == HealthConnectAvailability.AVAILABLE) {
            HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions()
        } else {
            emptySet()
        }
    }

    override suspend fun syncRunSession(runSessionDraft: RunSessionDraft): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException(
                "Milestone 5 尚未完成：目前僅支援 Health Connect 可用性與權限檢查。",
            ),
        )
    }
}
