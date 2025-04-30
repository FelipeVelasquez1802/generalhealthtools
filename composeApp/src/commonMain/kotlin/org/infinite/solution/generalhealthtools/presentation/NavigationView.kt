package org.infinite.solution.generalhealthtools.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.infinite.solution.generalhealthtools.presentation.dashboard.router.DashboardPath
import org.infinite.solution.generalhealthtools.presentation.dashboard.view.DashboardView

@Composable
internal fun NavigationView() {
    val navigationController = rememberNavController()
    val startView = DashboardPath.Dashboard
    NavHost(
        navController = navigationController,
        startDestination = startView.path
    ) {
        composable(route = DashboardPath.Dashboard.path) {
            DashboardView()
        }
    }
}