package com.example.android.architecture.blueprints.todoapp.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseRoute
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Length
import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
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

    override suspend fun syncRunSession(runSessionDraft: RunSessionDraft): Result<Unit> = runCatching {
        check(getAvailability() == HealthConnectAvailability.AVAILABLE) {
            "Health Connect 目前不可用，請先完成安裝或更新。"
        }

        val client = HealthConnectClient.getOrCreate(context)
        val startZoneOffset = ZoneId.systemDefault().rules.getOffset(runSessionDraft.startTime)
        val endZoneOffset = ZoneId.systemDefault().rules.getOffset(runSessionDraft.endTime)

        val route = ExerciseRoute(
            runSessionDraft.points.mapNotNull { point ->
                val timestamp = point.time ?: return@mapNotNull null
                ExerciseRoute.Location(
                    time = timestamp,
                    latitude = point.lat,
                    longitude = point.lng,
                    altitude = point.elevation?.let { Length.meters(it) },
                )
            },
        )

        val sessionRecord = ExerciseSessionRecord(
            startTime = runSessionDraft.startTime,
            startZoneOffset = startZoneOffset,
            endTime = runSessionDraft.endTime,
            endZoneOffset = endZoneOffset,
            metadata = Metadata.manualEntry(),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
            exerciseRoute = route,
            title = "GPX Imported Run",
            notes = "Imported by Step App",
        )

        client.insertRecords(listOf(sessionRecord))
        Unit
    }
}
