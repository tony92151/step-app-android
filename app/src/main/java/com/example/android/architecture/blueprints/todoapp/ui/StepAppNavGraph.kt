package com.example.android.architecture.blueprints.todoapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.architecture.blueprints.todoapp.auth.LoginRoute
import com.example.android.architecture.blueprints.todoapp.gpx.GpxImportRoute
import com.example.android.architecture.blueprints.todoapp.gpx.RunPreviewRoute

private object StepRoutes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val IMPORT = "import"
    const val PREVIEW = "preview"
    const val SYNC = "sync"
}

@Composable
fun StepAppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = StepRoutes.HOME,
        modifier = modifier,
    ) {
        composable(StepRoutes.HOME) {
            HomeScreen(
                onNavigateToLogin = { navController.navigate(StepRoutes.LOGIN) },
                onNavigateToImport = { navController.navigate(StepRoutes.IMPORT) },
                onNavigateToPreview = { navController.navigate(StepRoutes.PREVIEW) },
                onNavigateToSync = { navController.navigate(StepRoutes.SYNC) },
            )
        }
        composable(StepRoutes.LOGIN) {
            LoginRoute(onBack = { navController.popBackStack() })
        }
        composable(StepRoutes.IMPORT) {
            GpxImportRoute(
                onBack = { navController.popBackStack() },
                onNavigateToPreview = { navController.navigate(StepRoutes.PREVIEW) },
            )
        }
        composable(StepRoutes.PREVIEW) {
            RunPreviewRoute(
                onBack = { navController.popBackStack() },
                onNavigateToSync = { navController.navigate(StepRoutes.SYNC) },
            )
        }
        composable(StepRoutes.SYNC) {
            FeaturePlaceholderScreen(
                title = "Health Connect Sync",
                description = "Milestone 4-5: permission checks and data sync",
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToSync: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "GPX to Health Connect",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Milestone 0: project initialized with Compose and basic navigation.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onNavigateToLogin) { Text("Go to Login") }
            Button(onClick = onNavigateToImport) { Text("Go to GPX Import") }
            Button(onClick = onNavigateToPreview) { Text("Go to Run Preview") }
            Button(onClick = onNavigateToSync) { Text("Go to Health Connect Sync") }
        }
    }
}

@Composable
private fun FeaturePlaceholderScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onBack) { Text("Back") }
        }
    }
}
