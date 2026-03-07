package com.example.android.architecture.blueprints.todoapp.healthconnect

import androidx.test.core.app.ApplicationProvider
import com.example.android.architecture.blueprints.todoapp.domain.GpxTrackPoint
import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft
import com.example.android.architecture.blueprints.todoapp.domain.RunSummaryCalculator
import com.example.android.architecture.blueprints.todoapp.gpx.GpxImportRepository
import com.example.android.architecture.blueprints.todoapp.gpx.GpxParser
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthConnectCheckViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun refreshStatus_whenAvailableAndAlreadyGranted_marksReady() = runTest(testDispatcher) {
        val requiredPermissions = setOf("a", "b")
        val viewModel = HealthConnectCheckViewModel(
            healthConnectGateway = FakeHealthConnectGateway(
                availability = HealthConnectAvailability.AVAILABLE,
                requiredPermissions = requiredPermissions,
                grantedPermissions = requiredPermissions,
            ),
            gpxImportRepository = buildRepository(),
            runSummaryCalculator = RunSummaryCalculator(),
        )

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isAvailable).isTrue()
        assertThat(viewModel.uiState.value.hasAllPermissions).isTrue()
        assertThat(viewModel.uiState.value.message).isEqualTo("已完成授權，可開始同步。")
    }

    @Test
    fun onPermissionResult_whenPartialPermission_keepsPromptMessage() = runTest(testDispatcher) {
        val requiredPermissions = setOf("a", "b")
        val viewModel = HealthConnectCheckViewModel(
            healthConnectGateway = FakeHealthConnectGateway(
                availability = HealthConnectAvailability.AVAILABLE,
                requiredPermissions = requiredPermissions,
                grantedPermissions = emptySet(),
            ),
            gpxImportRepository = buildRepository(),
            runSummaryCalculator = RunSummaryCalculator(),
        )

        advanceUntilIdle()
        viewModel.onPermissionResult(setOf("a"))

        assertThat(viewModel.uiState.value.hasAllPermissions).isFalse()
        assertThat(viewModel.uiState.value.message).isEqualTo("尚未授予完整權限，請點擊按鈕完成授權。")
    }

    @Test
    fun refreshStatus_whenUnavailable_showsInstallGuidance() = runTest(testDispatcher) {
        val viewModel = HealthConnectCheckViewModel(
            healthConnectGateway = FakeHealthConnectGateway(
                availability = HealthConnectAvailability.NOT_INSTALLED,
                requiredPermissions = setOf("a"),
                grantedPermissions = emptySet(),
            ),
            gpxImportRepository = buildRepository(),
            runSummaryCalculator = RunSummaryCalculator(),
        )

        assertThat(viewModel.uiState.value.isAvailable).isFalse()
        assertThat(viewModel.uiState.value.message).isEqualTo("尚未安裝 Health Connect，請先安裝後再返回同步。")
    }

    private fun buildRepository(): GpxImportRepository {
        return GpxImportRepository(
            context = ApplicationProvider.getApplicationContext(),
            gpxParser = object : GpxParser {
                override fun parse(rawGpx: String): Result<List<GpxTrackPoint>> = Result.success(emptyList())
            },
        )
    }
}

private class FakeHealthConnectGateway(
    private val availability: HealthConnectAvailability,
    override val requiredPermissions: Set<String>,
    private val grantedPermissions: Set<String>,
) : HealthConnectGateway {

    override fun getAvailability(): HealthConnectAvailability = availability

    override suspend fun getGrantedPermissions(): Set<String> = grantedPermissions

    override suspend fun syncRunSession(runSessionDraft: RunSessionDraft): Result<Unit> {
        return Result.success(Unit)
    }
}
