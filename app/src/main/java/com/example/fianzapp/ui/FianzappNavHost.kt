package com.example.fianzapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fianzapp.SkeletonScreen
import com.example.fianzapp.ui.checklist.ChecklistScreen
import com.example.fianzapp.ui.deadline.DeadlineScreen
import com.example.fianzapp.ui.home.HomeScreen
import com.example.fianzapp.ui.lease.LeaseScreen
import com.example.fianzapp.ui.rooms.RoomsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object LeaseRoute
@Serializable object RoomsRoute
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
                onLease = { navController.navigate(LeaseRoute) },
                onRooms = { navController.navigate(RoomsRoute) },
                onChecklist = { navController.navigate(ChecklistRoute) },
                onDeadline = { navController.navigate(DeadlineRoute) },
                onSignatureDemo = { navController.navigate(SignatureDemoRoute) }
            )
        }
        composable<LeaseRoute> { LeaseScreen() }
        composable<RoomsRoute> { RoomsScreen(onOpenLease = { navController.navigate(LeaseRoute) }) }
        composable<ChecklistRoute> { ChecklistScreen() }
        composable<DeadlineRoute> { DeadlineScreen() }
        composable<SignatureDemoRoute> { SkeletonScreen() }
    }
}
