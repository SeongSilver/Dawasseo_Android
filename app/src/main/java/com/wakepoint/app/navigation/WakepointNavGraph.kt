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
import androidx.navigation.NavHostController
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
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
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
import com.wakepoint.app.feature.place.MyPlacesScreen
import com.wakepoint.app.feature.place.PlaceEditScreen
import com.wakepoint.app.feature.profile.ProfileScreen

private object AppRoute {
    const val Splash = "splash"
    const val Auth = "auth"
    const val SignUp = "sign-up"
    const val SoundList = "sound-list"

    /** 특정 알람의 사운드를 편집할 때는 alarmId 를 전달, 전역 기본 사운드는 생략. */
    fun soundList(alarmId: String? = null): String =
        if (alarmId.isNullOrBlank()) SoundList else "$SoundList?alarmId=$alarmId"

    const val MyPlaces = "my-places"

    /** 장소 등록·편집. 신규는 placeId 생략, 집 슬롯 등록은 home=true. */
    fun placeEdit(placeId: String? = null, home: Boolean = false): String =
        "place-edit?placeId=${placeId.orEmpty()}&home=$home"
}

/**
 * 하단 탭 간 이동. 홈을 백스택 루트로 유지하고, 벗어나는 탭의 상태(스크롤·ViewModel)를
 * 저장했다가 재방문 시 복원한다.
 *
 * 이전에는 popUpTo 대상이 그래프 시작점(스플래시)이었는데, 로그인 리다이렉트가 스플래시를
 * 스택에서 제거한 뒤라 popUpTo가 아무것도 하지 않았다. 그래서 저장/복원이 전혀 동작하지 않아
 * 탭을 누를 때마다 새 엔트리·새 ViewModel이 만들어졌고, 빈 초기 상태가 먼저 그려졌다가
 * 데이터가 늦게 도착하는 깜빡임과 백스택 무한 증가가 발생했다.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(MainRoute.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
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

/**
 * 인증 상태와 현재 화면을 보고 이동이 "필요할 때만" 목적지 라우트를 돌려준다(테스트 대상).
 *
 * - 세션 확인 중이거나 NavHost 가 아직 준비되지 않았으면(currentRoute == null) 대기.
 * - 로그인 상태인데 스플래시/로그인/가입 화면에 있으면 → 홈으로.
 * - 비로그인 상태인데 인증 영역 밖에 있으면(세션 만료·로그아웃) → 로그인 화면으로.
 * - 그 외에는 null: 사용자가 보던 화면을 그대로 유지한다.
 */
internal fun resolveAuthRedirect(
    isCheckingSession: Boolean,
    isAuthenticated: Boolean,
    currentRoute: String?
): String? {
    if (isCheckingSession || currentRoute == null) return null
    val onAuthFlow = currentRoute == AppRoute.Auth || currentRoute == AppRoute.SignUp
    return when {
        // 스플래시는 항상 상태가 확정되는 즉시 벗어난다
        currentRoute == AppRoute.Splash ->
            if (isAuthenticated) MainRoute.Home.route else AppRoute.Auth
        isAuthenticated && onAuthFlow -> MainRoute.Home.route
        !isAuthenticated && !onAuthFlow -> AppRoute.Auth
        else -> null
    }
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

    // 인증 상태에 따른 라우팅. 현재 화면이 이미 올바른 영역에 있으면 아무것도 하지 않는다(idempotent).
    // 이전에는 조건 없이 navigate(Home) 을 호출해, 초기 로딩이 끝나기 전에 다른 탭으로 이동했거나
    // 액티비티가 재생성된 경우(권한 설정 왕복, 다크모드 전환 등) 사용자를 지도 첫 화면으로
    // 강제로 되돌리는 문제가 있었다.
    LaunchedEffect(authUiState.isCheckingSession, authUiState.isAuthenticated, backStackEntry) {
        val targetRoute = resolveAuthRedirect(
            isCheckingSession = authUiState.isCheckingSession,
            isAuthenticated = authUiState.isAuthenticated,
            currentRoute = backStackEntry?.destination?.route
        )
        if (targetRoute != null) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.id) {
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
                    onAlarmCreated = { navController.navigateToTab(MainRoute.Alarms.route) }
                )
            }
            composable(MainRoute.Alarms.route) {
                AlarmsScreen(
                    onOpenSoundList = { alarmId ->
                        navController.navigate(AppRoute.soundList(alarmId))
                    },
                    onCreateAlarm = { navController.navigateToTab(MainRoute.Home.route) }
                )
            }
            composable(MainRoute.Friends.route) { FriendsScreen() }
            composable(MainRoute.Profile.route) {
                ProfileScreen(
                    onOpenSoundList = { navController.navigate(AppRoute.soundList()) },
                    onOpenMyPlaces = { navController.navigate(AppRoute.MyPlaces) },
                    onLogout = authViewModel::signOut
                )
            }
            composable(AppRoute.MyPlaces) {
                MyPlacesScreen(
                    onBack = { navController.popBackStack() },
                    onAddPlace = { isHome ->
                        navController.navigate(AppRoute.placeEdit(home = isHome))
                    },
                    onEditPlace = { place ->
                        navController.navigate(AppRoute.placeEdit(placeId = place.id))
                    }
                )
            }
            composable(
                route = "place-edit?placeId={placeId}&home={home}",
                arguments = listOf(
                    navArgument("placeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("home") {
                        type = NavType.StringType
                        defaultValue = "false"
                    }
                )
            ) {
                PlaceEditScreen(onBack = { navController.popBackStack() })
            }
        }

        if (showBottomBar) {
            WakepointTabBar(
                items = mainRoutes,
                currentRoute = currentRoute,
                onSelect = { item -> navController.navigateToTab(item.route) },
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
            .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = WakepointShape.full,
            color = WakepointCanvas,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, WakepointBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WakepointSpacing.sm),
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
            .clip(WakepointShape.full)
            .clickable(onClick = onClick)
            .padding(vertical = WakepointSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(WakepointShape.full)
                .background(if (selected) WakepointPrimaryLight else androidx.compose.ui.graphics.Color.Transparent),
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
