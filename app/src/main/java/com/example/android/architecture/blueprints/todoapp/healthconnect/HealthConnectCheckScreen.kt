package com.example.android.architecture.blueprints.todoapp.healthconnect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectCheckRoute(
    onBack: () -> Unit,
    viewModel: HealthConnectCheckViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    HealthConnectCheckScreen(
        uiState = uiState,
        onRefresh = viewModel::refreshStatus,
        onRequestPermission = {
            permissionLauncher.launch(uiState.requiredPermissions)
        },
        onSync = viewModel::syncLatestRun,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthConnectCheckScreen(
    uiState: HealthConnectCheckUiState,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onSync: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Connect 同步") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isSyncing) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium)

            // 區分「檢查狀態中」與「同步中」的 loading 狀態
            when {
                uiState.isSyncing -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "正在同步跑步資料…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                uiState.isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "正在檢查 Health Connect 狀態…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (uiState.isAvailable) {
                Button(
                    onClick = onRequestPermission,
                    enabled = !uiState.hasAllPermissions,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.hasAllPermissions) "已授權" else "請求 Health Connect 權限")
                }
                Button(
                    onClick = onSync,
                    enabled = uiState.hasAllPermissions && !uiState.isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("同步跑步資料")
                }
            } else {
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.apps.healthdata"),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("前往安裝 / 更新 Health Connect")
                }
            }

            uiState.syncErrorMessage?.let {
                Text(
                    text = "錯誤詳情：$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onRefresh,
                enabled = !uiState.isSyncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("重新檢查")
            }
        }
    }
}
