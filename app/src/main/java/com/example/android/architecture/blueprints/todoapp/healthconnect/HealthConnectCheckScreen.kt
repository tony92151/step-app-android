package com.example.android.architecture.blueprints.todoapp.healthconnect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel

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
        onBack = onBack,
    )
}

@Composable
private fun HealthConnectCheckScreen(
    uiState: HealthConnectCheckUiState,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Health Connect Sync", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Milestone 4: 完成 SDK 狀態檢查、權限請求與導引流程",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium)

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            if (uiState.isAvailable) {
                Button(onClick = onRequestPermission, enabled = !uiState.hasAllPermissions) {
                    Text(if (uiState.hasAllPermissions) "已授權" else "請求 Health Connect 權限")
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
                ) {
                    Text("前往安裝 / 更新 Health Connect")
                }
            }

            Button(onClick = onRefresh) {
                Text("重新檢查")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
