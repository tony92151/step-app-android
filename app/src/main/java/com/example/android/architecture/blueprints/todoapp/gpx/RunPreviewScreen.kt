package com.example.android.architecture.blueprints.todoapp.gpx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunPreviewScreen(
    uiState: RunPreviewUiState,
    onBack: () -> Unit,
    onNavigateToSync: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("跑步摘要預覽") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.fileName?.let {
                        RunDataRow(label = "檔案", value = it)
                        HorizontalDivider()
                    }
                    uiState.startTime?.let {
                        RunDataRow(label = "開始時間", value = it)
                    }
                    uiState.endTime?.let {
                        RunDataRow(label = "結束時間", value = it)
                    }
                    RunDataRow(label = "點位總數", value = "${uiState.pointCount}")
                    uiState.totalDistance?.let {
                        RunDataRow(label = "總距離", value = it)
                    }
                    uiState.averagePace?.let {
                        RunDataRow(label = "平均配速", value = it)
                    }
                }
            }

            uiState.missingDataMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = onNavigateToSync,
                enabled = uiState.canSync,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("同步到 Health Connect")
            }
        }
    }
}

@Composable
private fun RunDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
