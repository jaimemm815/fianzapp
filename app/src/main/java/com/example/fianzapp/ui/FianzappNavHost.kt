package com.example.fianzapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fianzapp.R
import com.example.fianzapp.SkeletonScreen
import com.example.fianzapp.ui.checklist.ChecklistScreen
import com.example.fianzapp.ui.deadline.DeadlineScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object ChecklistRoute
@Serializable object DeadlineRoute
@Serializable object SignatureDemoRoute

@Composable
fun FianzappNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = HomeRoute, modifier = modifier) {
        composable<HomeRoute> {
            HomeScreen(
                onChecklist = { navController.navigate(ChecklistRoute) },
                onDeadline = { navController.navigate(DeadlineRoute) },
                onSignatureDemo = { navController.navigate(SignatureDemoRoute) }
            )
        }
        composable<ChecklistRoute> { ChecklistScreen() }
        composable<DeadlineRoute> { DeadlineScreen() }
        composable<SignatureDemoRoute> { SkeletonScreen() }
    }
}

/** Temporary home: one button per screen, until the group decides the real flow. */
@Composable
private fun HomeScreen(
    onChecklist: () -> Unit,
    onDeadline: () -> Unit,
    onSignatureDemo: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onChecklist, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_checklist))
        }
        Button(onClick = onDeadline, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_deadline))
        }
        Button(onClick = onSignatureDemo, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_signature_demo))
        }
    }
}
