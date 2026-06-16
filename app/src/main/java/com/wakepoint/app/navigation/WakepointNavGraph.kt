package com.wakepoint.app.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.feature.alarms.AlarmsScreen
import com.wakepoint.app.feature.alarms.SoundListScreen
import com.wakepoint.app.feature.auth.AuthScreen
import com.wakepoint.app.feature.auth.AuthViewModel
import com.wakepoint.app.feature.auth.SignUpScreen
import com.wakepoint.app.feature.auth.SplashScreen
import com.wakepoint.app.feature.friends.FriendsScreen
import com.wakepoint.app.feature.home.HomeScreen
import com.wakepoint.app.feature.profile.ProfileScreen

private object AppRoute {
    const val Splash = "splash"
    const val Auth = "auth"
    const val SignUp = "sign-up"
    const val SoundList = "sound-list"
}

private enum class MainRoute(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
) {
    Home("home", R.string.tab_home, Icons.Rounded.Map),
    Alarms("alarms", R.string.tab_alarms, Icons.Rounded.Notifications),
    Friends("friends", R.string.tab_friends, Icons.Rounded.Groups),
    Profile("profile", R.string.tab_profile, Icons.Rounded.Person)
}

@Composable
fun WakepointApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    val mainRoutes = MainRoute.entries
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainRoute.Home.route
    val showBottomBar = mainRoutes.any { it.route == currentRoute }

    LaunchedEffect(authUiState.isCheckingSession, authUiState.isAuthenticated) {
        if (!authUiState.isCheckingSession) {
            val targetRoute = if (authUiState.isAuthenticated) {
                MainRoute.Home.route
            } else {
                AppRoute.Auth
            }
            navController.navigate(targetRoute) {
                popUpTo(AppRoute.Splash) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = WakepointCanvas,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = WakepointCanvas) {
                    mainRoutes.forEach { item ->
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
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = label
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WakepointPrimary,
                                selectedTextColor = WakepointPrimary,
                                unselectedIconColor = WakepointInk,
                                unselectedTextColor = WakepointInk,
                                indicatorColor = WakepointCanvas,
                                disabledIconColor = WakepointMuted,
                                disabledTextColor = WakepointMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Splash) { SplashScreen() }
            composable(AppRoute.Auth) {
                AuthScreen(
                    uiState = authUiState,
                    onLoginEmailChange = authViewModel::updateLoginEmail,
                    onLoginPasswordChange = authViewModel::updateLoginPassword,
                    onSignIn = authViewModel::signIn,
                    onSignUp = { navController.navigate(AppRoute.SignUp) }
                )
            }
            composable(AppRoute.SignUp) {
                SignUpScreen(
                    uiState = authUiState,
                    onEmailChange = authViewModel::updateSignUpEmail,
                    onPasswordChange = authViewModel::updateSignUpPassword,
                    onPasswordConfirmChange = authViewModel::updateSignUpPasswordConfirm,
                    onNameChange = authViewModel::updateSignUpName,
                    onSubmit = authViewModel::signUp,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoute.SoundList) {
                SoundListScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoute.Home.route) { HomeScreen() }
            composable(MainRoute.Alarms.route) {
                AlarmsScreen(onOpenSoundList = { navController.navigate(AppRoute.SoundList) })
            }
            composable(MainRoute.Friends.route) { FriendsScreen() }
            composable(MainRoute.Profile.route) {
                ProfileScreen(
                    email = authUiState.session?.email.orEmpty(),
                    onLogout = authViewModel::signOut
                )
            }
        }
    }
}
