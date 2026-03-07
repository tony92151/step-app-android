package com.example.android.architecture.blueprints.todoapp.gpx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.domain.RunSessionDraft
import com.example.android.architecture.blueprints.todoapp.domain.RunSummaryCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RunPreviewViewModel @Inject constructor(
    private val repository: GpxImportRepository,
    private val runSummaryCalculator: RunSummaryCalculator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunPreviewUiState())
    val uiState: StateFlow<RunPreviewUiState> = _uiState.asStateFlow()

    init {
        observeLatestImport()
    }

    private fun observeLatestImport() {
        viewModelScope.launch {
            repository.latestImport.collect { latestImport ->
                if (latestImport == null) {
                    _uiState.value = RunPreviewUiState(
                        errorMessage = "尚未匯入 GPX，請先回上一頁選擇檔案",
                    )
                    return@collect
                }

                val points = latestImport.points
                val missingTimestampCount = points.count { it.time == null }

                if (missingTimestampCount > 0) {
                    _uiState.value = RunPreviewUiState(
                        fileName = latestImport.fileName,
                        pointCount = points.size,
                        missingDataMessage = "有 $missingTimestampCount 個點位缺少時間，無法同步",
                        canSync = false,
                    )
                    return@collect
                }

                val summaryResult = runSummaryCalculator.calculate(points)
                _uiState.update {
                    summaryResult.fold(
                        onSuccess = { draft -> draft.toUiState(fileName = latestImport.fileName) },
                        onFailure = { error ->
                            RunPreviewUiState(
                                fileName = latestImport.fileName,
                                pointCount = points.size,
                                errorMessage = error.message ?: "無法建立跑步摘要",
                                canSync = false,
                            )
                        },
                    )
                }
            }
        }
    }

    private fun RunSessionDraft.toUiState(fileName: String): RunPreviewUiState {
        return RunPreviewUiState(
            fileName = fileName,
            startTime = dateTimeFormatter.format(startTime.atZone(ZoneId.systemDefault())),
            endTime = dateTimeFormatter.format(endTime.atZone(ZoneId.systemDefault())),
            pointCount = points.size,
            totalDistance = String.format(Locale.US, "%.2f km", totalDistanceMeters / 1000.0),
            averagePace = averagePaceSecPerKm?.let { paceInSec ->
                val minutePart = (paceInSec / 60).toInt()
                val secondPart = (paceInSec % 60).toInt()
                String.format(Locale.US, "%d:%02d /km", minutePart, secondPart)
            } ?: "--",
            canSync = true,
        )
    }

    private companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}

data class RunPreviewUiState(
    val fileName: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val pointCount: Int = 0,
    val totalDistance: String? = null,
    val averagePace: String? = null,
    val missingDataMessage: String? = null,
    val errorMessage: String? = null,
    val canSync: Boolean = false,
)
