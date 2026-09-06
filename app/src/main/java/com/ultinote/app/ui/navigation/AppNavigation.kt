package com.ultinote.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ultinote.app.data.local.KomorebiRepository
import com.ultinote.app.data.model.CoverStyle
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.ui.screens.CalendarPlannerScreen
import com.ultinote.app.ui.screens.LibraryScreen
import com.ultinote.app.ui.screens.NoteEditorScreen
import com.ultinote.app.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: String) = "editor/$noteId"
    }
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}

private fun NavDestination.toRoute(): String = when (this) {
    NavDestination.LIBRARY -> Screen.Library.route
    NavDestination.CALENDAR -> Screen.Calendar.route
    NavDestination.FOLDERS -> Screen.Library.route
    NavDestination.THEMES -> Screen.Settings.route
}

private fun routeToDestination(route: String?): NavDestination = when {
    route == Screen.Calendar.route -> NavDestination.CALENDAR
    route == Screen.Settings.route -> NavDestination.THEMES
    route?.startsWith("editor/") == true -> NavDestination.LIBRARY
    else -> NavDestination.LIBRARY
}

private val mainTabRoutes = setOf(
    Screen.Library.route,
    Screen.Calendar.route,
    Screen.Settings.route
)

@Composable
fun KomorebiNavGraph(
    repository: KomorebiRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showMainShell = currentRoute in mainTabRoutes
    val currentDestination = routeToDestination(currentRoute)
    val coroutineScope = rememberCoroutineScope()
    var scrollToFoldersTrigger by remember { mutableStateOf(0) }

    fun navigateTo(dest: NavDestination) {
        val targetRoute = dest.toRoute()
        if (dest == NavDestination.FOLDERS) {
            if (currentRoute != Screen.Library.route) {
                navController.navigate(Screen.Library.route) {
                    popUpTo(Screen.Library.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            scrollToFoldersTrigger++
            return
        }
        if (currentRoute == targetRoute) return
        navController.navigate(targetRoute) {
            popUpTo(Screen.Library.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ResponsiveNavigationScaffold(
        showNavigation = showMainShell,
        currentDestination = currentDestination,
        onNavigate = { navigateTo(it) },
        onQuickCreateNote = {
            coroutineScope.launch {
                val newNoteId = repository.createNote(
                    title = "Untitled Notebook",
                    folderId = null,
                    coverStyle = CoverStyle.MINIMAL_MATCHA,
                    template = PaperTemplate.RULED
                )
                navController.navigate(Screen.Editor.createRoute(newNoteId))
            }
        }
    ) { isLandscape ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(androidx.compose.animation.core.tween(220)) + slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = androidx.compose.animation.core.tween(320)
                )
            },
            exitTransition = {
                fadeOut(androidx.compose.animation.core.tween(180)) + slideOutHorizontally(
                    targetOffsetX = { -it / 6 },
                    animationSpec = androidx.compose.animation.core.tween(280)
                )
            },
            popEnterTransition = {
                fadeIn(androidx.compose.animation.core.tween(220)) + slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = androidx.compose.animation.core.tween(320)
                )
            },
            popExitTransition = {
                fadeOut(androidx.compose.animation.core.tween(180)) + slideOutHorizontally(
                    targetOffsetX = { it / 6 },
                    animationSpec = androidx.compose.animation.core.tween(280)
                )
            }
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    repository = repository,
                    onOpenNote = { noteId ->
                        navController.navigate(Screen.Editor.createRoute(noteId))
                    },
                    onNavigateToCalendar = { navigateTo(NavDestination.CALENDAR) },
                    onNavigateToSettings = { navigateTo(NavDestination.THEMES) },
                    showEmbeddedNavigation = !showMainShell,
                    isLandscapeShell = isLandscape && showMainShell,
                    scrollToFoldersTrigger = scrollToFoldersTrigger
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(380)
                    ) + fadeIn(androidx.compose.animation.core.tween(200))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(340)
                    ) + fadeOut(androidx.compose.animation.core.tween(180))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = androidx.compose.animation.core.tween(320)
                    ) + fadeIn(androidx.compose.animation.core.tween(180))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(340)
                    ) + fadeOut(androidx.compose.animation.core.tween(180))
                }
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
                    },
                    showBackNavigation = !showMainShell
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    showBackNavigation = !showMainShell
                )
            }
        }
    }
}
