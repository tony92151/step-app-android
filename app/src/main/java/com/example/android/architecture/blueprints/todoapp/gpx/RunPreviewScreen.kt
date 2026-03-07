package com.example.android.architecture.blueprints.todoapp.gpx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RunPreviewRoute(
    onBack: () -> Unit,
    onNavigateToSync: () -> Unit,
    viewModel: RunPreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    RunPreviewScreen(
        uiState = uiState,
        onBack = onBack,
        onNavigateToSync = onNavigateToSync,
    )
}

@Composable
private fun RunPreviewScreen(
    uiState: RunPreviewUiState,
    onBack: () -> Unit,
    onNavigateToSync: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Run Preview", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Milestone 3: 同步前檢視跑步摘要，確認資料完整性",
                style = MaterialTheme.typography.bodyMedium,
            )

            uiState.fileName?.let { Text("檔案：$it", style = MaterialTheme.typography.bodyMedium) }
            uiState.startTime?.let { Text("開始時間：$it", style = MaterialTheme.typography.bodyMedium) }
            uiState.endTime?.let { Text("結束時間：$it", style = MaterialTheme.typography.bodyMedium) }
            Text("點位總數：${uiState.pointCount}", style = MaterialTheme.typography.bodyMedium)
            uiState.totalDistance?.let { Text("總距離：$it", style = MaterialTheme.typography.bodyMedium) }
            uiState.averagePace?.let { Text("平均配速：$it", style = MaterialTheme.typography.bodyMedium) }

            uiState.missingDataMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            uiState.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = onNavigateToSync,
                enabled = uiState.canSync,
            ) {
                Text("同步到 Health Connect")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
