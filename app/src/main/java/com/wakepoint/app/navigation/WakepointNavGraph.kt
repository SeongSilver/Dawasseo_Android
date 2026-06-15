package com.wakepoint.app.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPrimaryActive
import com.wakepoint.app.core.design.WakepointTabBar
import com.wakepoint.app.feature.alarms.AlarmsScreen
import com.wakepoint.app.feature.friends.FriendsScreen
import com.wakepoint.app.feature.home.HomeScreen
import com.wakepoint.app.feature.profile.ProfileScreen

private enum class MainRoute(
    val route: String,
    @StringRes val labelResId: Int
) {
    Home("home", R.string.tab_home),
    Alarms("alarms", R.string.tab_alarms),
    Friends("friends", R.string.tab_friends),
    Profile("profile", R.string.tab_profile)
}

@Composable
fun WakepointApp() {
    val navController = rememberNavController()
    val routes = MainRoute.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainRoute.Home.route

    Scaffold(
        containerColor = WakepointCanvas,
        bottomBar = {
            NavigationBar(containerColor = WakepointTabBar) {
                routes.forEach { item ->
                    val label = stringResource(item.labelResId)
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(label) },
                        icon = { Text(label.take(1)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WakepointPrimaryActive,
                            selectedTextColor = WakepointPrimaryActive,
                            unselectedIconColor = WakepointMuted,
                            unselectedTextColor = WakepointMuted,
                            indicatorColor = WakepointTabBar
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainRoute.Home.route) { HomeScreen() }
            composable(MainRoute.Alarms.route) { AlarmsScreen() }
            composable(MainRoute.Friends.route) { FriendsScreen() }
            composable(MainRoute.Profile.route) { ProfileScreen() }
        }
    }
}
