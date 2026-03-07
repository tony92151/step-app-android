package com.example.android.architecture.blueprints.todoapp.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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

@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Google Login", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Milestone 1: Credential Manager + Google Sign-In",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            Button(onClick = onSignIn, enabled = !uiState.isLoading) {
                Text("Sign in with Google")
            }
            Button(
                onClick = onSignOut,
                enabled = !uiState.isLoading && uiState.user != null,
            ) {
                Text("Sign out")
            }
            uiState.user?.let { user ->
                Text(text = "User ID: ${user.id}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Display name: ${user.displayName ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(text = "Email: ${user.email ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.message?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onBack, enabled = !uiState.isLoading) {
                Text("Back")
            }
        }
    }
}
