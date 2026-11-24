package com.example.fianzapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.fianzapp.ui.camera.CameraScreen
import com.example.fianzapp.ui.checklist.ChecklistScreen
import com.example.fianzapp.ui.deadline.DeadlineScreen
import com.example.fianzapp.ui.home.HomeScreen
import com.example.fianzapp.ui.landlord.LandlordScreen
import com.example.fianzapp.ui.lease.LeaseScreen
import com.example.fianzapp.ui.report.ReportScreen
import com.example.fianzapp.ui.room.RoomScreen
import com.example.fianzapp.ui.rooms.RoomsScreen
import com.example.fianzapp.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object LeaseRoute
@Serializable object RoomsRoute
/** Everything in a route is text: the id travels as a Long and is read back with toRoute. */
@Serializable data class RoomRoute(val roomId: Long)
/**
 * A8: the camera for one photo. [itemId] is -1 for a damage photo, which carries its [note].
 * [phase] is the name of a Phase (MOVE_IN or MOVE_OUT).
 */
@Serializable data class CameraRoute(
    val roomId: Long,
    val itemId: Int,
    val phase: String,
    val note: String = ""
)
@Serializable object ChecklistRoute
@Serializable object DeadlineRoute
@Serializable object ReportRoute
@Serializable object SettingsRoute
@Serializable object LandlordRoute

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
                onReport = { navController.navigate(ReportRoute) },
                onLandlord = { navController.navigate(LandlordRoute) },
                onSettings = { navController.navigate(SettingsRoute) }
            )
        }
        composable<LeaseRoute> { LeaseScreen() }
        composable<RoomsRoute> {
            RoomsScreen(
                onOpenLease = { navController.navigate(LeaseRoute) },
                onOpenRoom = { roomId -> navController.navigate(RoomRoute(roomId)) }
            )
        }
        composable<RoomRoute> { entry ->
            val roomId = entry.toRoute<RoomRoute>().roomId
            RoomScreen(
                onTakePhoto = { itemId, phase ->
                    navController.navigate(CameraRoute(roomId, itemId, phase.name))
                },
                onAddDamage = { note, phase ->
                    navController.navigate(CameraRoute(roomId, itemId = -1, phase = phase.name, note = note))
                }
            )
        }
        composable<CameraRoute> {
            CameraScreen(onPhotoSaved = { navController.popBackStack() })
        }
        composable<ChecklistRoute> { ChecklistScreen() }
        composable<DeadlineRoute> { DeadlineScreen(onOpenLease = { navController.navigate(LeaseRoute) }) }
        composable<ReportRoute> { ReportScreen(onOpenLease = { navController.navigate(LeaseRoute) }) }
        composable<SettingsRoute> { SettingsScreen() }
        composable<LandlordRoute> { LandlordScreen() }
    }
}
