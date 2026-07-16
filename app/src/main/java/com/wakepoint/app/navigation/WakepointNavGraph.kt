package com.wakepoint.app.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointBorder
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointIconInactive
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryTint10
import com.wakepoint.app.feature.alarms.AlarmsScreen
import com.wakepoint.app.feature.alarms.SoundListScreen
import com.wakepoint.app.feature.auth.AuthScreen
import com.wakepoint.app.feature.auth.AuthViewModel
import com.wakepoint.app.core.permission.AlarmDisplayPermissionGate
import com.wakepoint.app.core.permission.StartupPermissionRequester
import com.wakepoint.app.core.sync.AppSyncViewModel
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

    /** 특정 알람의 사운드를 편집할 때는 alarmId 를 전달, 전역 기본 사운드는 생략. */
    fun soundList(alarmId: String? = null): String =
        if (alarmId.isNullOrBlank()) SoundList else "$SoundList?alarmId=$alarmId"
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

    // 앱 초기 실행 시 필요한 권한을 한 번에 요청 (미허용분은 기능 사용 시점에 재요청)
    StartupPermissionRequester()
    // 로그인 후, 도착 알람이 즉시 전체화면으로 뜨도록 특수 권한을 1회 안내
    AlarmDisplayPermissionGate(enabled = authUiState.isAuthenticated && showBottomBar)

    // 앱이 포그라운드로 돌아올 때 서버의 최신 알람(친구 발신/타 기기)을 로컬에 반영
    val appSyncViewModel: AppSyncViewModel = hiltViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authUiState.isAuthenticated) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && authUiState.isAuthenticated) {
                appSyncViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(AppRoute.Splash) { SplashScreen() }
            composable(AppRoute.Auth) {
                AuthScreen(
                    uiState = authUiState,
                    onLoginEmailChange = authViewModel::updateLoginEmail,
                    onLoginPasswordChange = authViewModel::updateLoginPassword,
                    onSignIn = authViewModel::signIn,
                    onKakaoLogin = authViewModel::kakaoOAuthUrl,
                    onSignUp = { navController.navigate(AppRoute.SignUp) },
                    onForgotPassword = authViewModel::sendPasswordReset,
                    onDismissMessage = authViewModel::consumeMessage
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
                    onBack = { navController.popBackStack() },
                    onDismissMessage = authViewModel::consumeMessage
                )
            }
            composable(
                route = "${AppRoute.SoundList}?alarmId={alarmId}",
                arguments = listOf(
                    navArgument("alarmId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                SoundListScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoute.Home.route) {
                HomeScreen(
                    onAlarmCreated = {
                        navController.navigate(MainRoute.Alarms.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(MainRoute.Alarms.route) {
                AlarmsScreen(
                    onOpenSoundList = { alarmId ->
                        navController.navigate(AppRoute.soundList(alarmId))
                    },
                    onCreateAlarm = {
                        navController.navigate(MainRoute.Home.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(MainRoute.Friends.route) { FriendsScreen() }
            composable(MainRoute.Profile.route) {
                ProfileScreen(
                    onOpenSoundList = { navController.navigate(AppRoute.soundList()) },
                    onLogout = authViewModel::signOut
                )
            }
        }

        if (showBottomBar) {
            WakepointTabBar(
                items = mainRoutes,
                currentRoute = currentRoute,
                onSelect = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun WakepointTabBar(
    items: List<MainRoute>,
    currentRoute: String,
    onSelect: (MainRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            color = WakepointCanvas,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, WakepointBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    WakepointTabItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WakepointTabItem(
    item: MainRoute,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(item.labelResId)
    val tint = if (selected) WakepointPrimary else WakepointIconInactive
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (selected) WakepointPrimaryTint10 else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold
                else androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = tint
        )
    }
}
