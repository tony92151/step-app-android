package com.example.android.architecture.blueprints.todoapp.gpx

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GpxImportRoute(
    onBack: () -> Unit,
    onNavigateToPreview: () -> Unit,
    viewModel: GpxImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::import)
    }

    GpxImportScreen(
        uiState = uiState,
        onPickGpx = {
            launcher.launch(
                arrayOf(
                    "application/gpx+xml",
                    "application/xml",
                    "text/xml",
                    "*/*",
                )
            )
        },
        onBack = onBack,
        onNavigateToPreview = onNavigateToPreview,
    )
}

@Composable
private fun GpxImportScreen(
    uiState: GpxImportUiState,
    onPickGpx: () -> Unit,
    onBack: () -> Unit,
    onNavigateToPreview: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "GPX Import", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Milestone 2: 使用檔案選擇器匯入 GPX 並解析點位資料",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            Button(onClick = onPickGpx, enabled = !uiState.isLoading) {
                Text("選擇 GPX 檔案")
            }
            uiState.fileName?.let {
                Text("檔案：$it", style = MaterialTheme.typography.bodyMedium)
                Text("點位總數：${uiState.pointCount}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "含時間戳記點數：${uiState.pointWithTimestampCount}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            uiState.successMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            uiState.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onNavigateToPreview,
                enabled = uiState.fileName != null && uiState.errorMessage == null && !uiState.isLoading,
            ) {
                Text("前往跑步摘要預覽")
            }
            Button(onClick = onBack, enabled = !uiState.isLoading) {
                Text("Back")
            }
        }
    }
}
