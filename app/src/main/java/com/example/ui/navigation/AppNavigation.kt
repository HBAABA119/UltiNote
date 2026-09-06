package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.KomorebiRepository
import com.example.ui.screens.CalendarPlannerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: String) = "editor/$noteId"
    }
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}

@Composable
fun KomorebiNavGraph(
    repository: KomorebiRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Library.route
    ) {
        composable(Screen.Library.route) {
            LibraryScreen(
                repository = repository,
                onOpenNote = { noteId ->
                    navController.navigate(Screen.Editor.createRoute(noteId))
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteEditorScreen(
                noteId = noteId,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarPlannerScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenNote = { noteId ->
                    navController.navigate(Screen.Editor.createRoute(noteId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
