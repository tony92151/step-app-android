package com.example.android.architecture.blueprints.todoapp.healthconnect

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
class HealthConnectCheckViewModel @Inject constructor(
    private val healthConnectGateway: HealthConnectGateway,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthConnectCheckUiState())
    val uiState: StateFlow<HealthConnectCheckUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        val availability = healthConnectGateway.getAvailability()
        _uiState.update {
            it.copy(
                availability = availability,
                requiredPermissions = healthConnectGateway.requiredPermissions,
                isLoading = availability == HealthConnectAvailability.AVAILABLE,
                message = availability.toMessage(),
            )
        }

        if (availability == HealthConnectAvailability.AVAILABLE) {
            viewModelScope.launch {
                val granted = healthConnectGateway.getGrantedPermissions()
                _uiState.update {
                    it.copy(
                        grantedPermissions = granted,
                        isLoading = false,
                        message = granted.toMessage(healthConnectGateway.requiredPermissions),
                    )
                }
            }
        }
    }

    fun onPermissionResult(grantedPermissions: Set<String>) {
        _uiState.update {
            it.copy(
                grantedPermissions = grantedPermissions,
                message = grantedPermissions.toMessage(healthConnectGateway.requiredPermissions),
            )
        }
    }

    private fun HealthConnectAvailability.toMessage(): String {
        return when (this) {
            HealthConnectAvailability.AVAILABLE -> "Health Connect 可用，正在檢查授權狀態..."
            HealthConnectAvailability.NOT_INSTALLED -> "尚未安裝 Health Connect，請先安裝後再返回同步。"
            HealthConnectAvailability.UPDATE_REQUIRED -> "Health Connect 需要更新，請更新後再返回同步。"
            HealthConnectAvailability.UNAVAILABLE -> "目前裝置不支援 Health Connect。"
        }
    }

    private fun Set<String>.toMessage(requiredPermissions: Set<String>): String {
        return if (containsAll(requiredPermissions)) {
            "已完成授權，可返回同步流程。"
        } else {
            "尚未授予完整權限，請點擊按鈕完成授權。"
        }
    }
}

data class HealthConnectCheckUiState(
    val availability: HealthConnectAvailability = HealthConnectAvailability.UNAVAILABLE,
    val requiredPermissions: Set<String> = emptySet(),
    val grantedPermissions: Set<String> = emptySet(),
    val message: String = "",
    val isLoading: Boolean = false,
) {
    val isAvailable: Boolean = availability == HealthConnectAvailability.AVAILABLE
    val hasAllPermissions: Boolean = grantedPermissions.containsAll(requiredPermissions)
}
