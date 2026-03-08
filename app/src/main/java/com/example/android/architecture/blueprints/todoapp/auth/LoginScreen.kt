package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRoute(
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = findActivity(LocalContext.current)

    LoginScreen(
        uiState = uiState,
        onBack = onBack,
        onSignIn = {
            activity?.let(viewModel::signIn)
        },
        onSignOut = {
            activity?.let(viewModel::signOut)
        },
    )
}

private tailrec fun findActivity(context: Context): Activity? {
    return when (context) {
        is Activity -> context
        is ContextWrapper -> findActivity(context.baseContext)
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google 登入") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            Button(onClick = onSignIn, enabled = !uiState.isLoading) {
                Text("使用 Google 帳號登入")
            }
            Button(
                onClick = onSignOut,
                enabled = !uiState.isLoading && uiState.user != null,
            ) {
                Text("登出")
            }
            uiState.user?.let { user ->
                Text(text = "使用者 ID：${user.id}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "顯示名稱：${user.displayName ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(text = "電子郵件：${user.email ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.message?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
