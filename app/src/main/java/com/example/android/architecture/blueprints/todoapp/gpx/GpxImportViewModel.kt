package com.example.android.architecture.blueprints.todoapp.gpx

import android.net.Uri
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
class GpxImportViewModel @Inject constructor(
    private val repository: GpxImportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GpxImportUiState())
    val uiState: StateFlow<GpxImportUiState> = _uiState.asStateFlow()

    fun import(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            val result = repository.importFromUri(uri)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { imported ->
                        currentState.copy(
                            isLoading = false,
                            fileName = imported.fileName,
                            pointCount = imported.points.size,
                            pointWithTimestampCount = imported.points.count { it.time != null },
                            successMessage = "GPX 解析成功，可前往跑步摘要預覽",
                        )
                    },
                    onFailure = { error ->
                        currentState.copy(
                            isLoading = false,
                            fileName = null,
                            pointCount = 0,
                            pointWithTimestampCount = 0,
                            errorMessage = error.message ?: "GPX 解析失敗",
                        )
                    }
                )
            }
        }
    }
}

data class GpxImportUiState(
    val isLoading: Boolean = false,
    val fileName: String? = null,
    val pointCount: Int = 0,
    val pointWithTimestampCount: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
