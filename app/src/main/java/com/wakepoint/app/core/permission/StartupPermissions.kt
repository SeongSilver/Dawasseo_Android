package com.wakepoint.app.core.permission

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointConfirmDialog

/**
 * 앱 초기 실행 시 필요한 런타임 권한을 한 번에 요청한다.
 *
 * - 위치(정확/대략) → 허용되면 백그라운드 위치를 이어서 요청(Android 10+ 규칙상 2단계)
 * - 알림(Android 13+), 마이크(녹음), 오디오 읽기(사운드 가져오기, Android 13+)
 *
 * 여기서 거부/미허용된 권한은 기존처럼 각 기능을 사용하는 시점에 다시 요청된다.
 * 특수 접근 권한(전체 화면 알림, 백그라운드 위치의 "항상 허용")은 시스템 정책상
 * 별도 화면에서만 부여되므로 여기서 강제하지 않는다.
 */
@Composable
fun StartupPermissionRequester() {
    val context = LocalContext.current

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 결과와 무관 — 사용 시점에 재요청됨 */ }

    val startupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val hasForeground = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (
            hasForeground &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    // 최초 1회만 요청 (LaunchedEffect(Unit) 은 컴포지션 수명 동안 1회 실행)
    LaunchedEffect(Unit) {
        val missing = context.missingStartupPermissions()
        if (missing.isNotEmpty()) {
            startupLauncher.launch(missing.toTypedArray())
        } else if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
}

/**
 * 도착 알람이 백그라운드/잠금화면/다른 앱 위에서도 즉시 전체화면으로 뜨도록 하는
 * 특수 접근 권한(전체 화면 알림 · 다른 앱 위에 표시)을 1회 안내한다.
 *
 * 알람을 직접 만들지 않고 친구에게서 받기만 하는 사용자도 이 권한이 필요하므로,
 * 로그인 후 메인 화면 진입 시 아직 권한이 없으면 안내 다이얼로그를 보여준다.
 *
 * 안내는 설치당 **딱 한 번**만 띄운다. "나중에"든 "권한 설정"이든 사용자가 한 번
 * 응답했으면 영구 플래그를 저장하고 다시 묻지 않는다. (이전에는 "권한 설정"으로
 * 이동한 뒤 미허용분이 남아 있으면 매 실행마다 다시 안내해, 오버레이 권한을 허용하지
 * 않은 사용자에게 전체화면 권한을 무한 반복 요청하는 문제가 있었다.)
 *
 * 중요: 이 안내는 이번 실행(프로세스) 동안 **딱 한 번만** 평가한다. enabled 는
 * 메인 탭↔서브화면(사운드 목록/알람 편집) 이동 시 showBottomBar 를 따라 여러 번
 * false↔true 로 토글되는데, 그때마다 다시 묻지 않도록 세션 가드(evaluated)를 둔다.
 * 이 가드가 없으면 "알람 설정에 들어갈 때마다 전체화면 권한을 반복 요청"하게 된다.
 *
 * @param enabled 로그인 완료 등 안내를 띄워도 되는 조건. false면 아무것도 하지 않는다.
 */
@Composable
fun AlarmDisplayPermissionGate(enabled: Boolean) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    // 이번 실행 동안 1회만 평가. 화면 회전/액티비티 재생성에도 유지되도록 rememberSaveable 사용.
    var evaluated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled || evaluated) return@LaunchedEffect
        evaluated = true
        showDialog = shouldPromptAlarmDisplay(
            enabled = true,
            evaluatedThisSession = false,
            dismissedPermanently = context.isAlarmDisplayPrompted(),
            permissionMissing = context.needsAlarmDisplayPermission()
        )
    }

    if (showDialog) {
        WakepointConfirmDialog(
            title = stringResource(R.string.perm_alarm_display_title),
            message = stringResource(R.string.perm_alarm_display_message),
            confirmText = stringResource(R.string.perm_alarm_display_confirm),
            cancelText = stringResource(R.string.perm_alarm_display_later),
            onConfirm = {
                showDialog = false
                // 설정으로 이동한 경우에도 영구 플래그를 저장해 다음 실행에서 재안내하지 않는다.
                context.markAlarmDisplayPrompted()
                context.openAlarmDisplaySettings()
            },
            onDismiss = {
                showDialog = false
                context.markAlarmDisplayPrompted()
            }
        )
    }
}

/**
 * 전체화면 알람 권한 안내를 띄울지 결정하는 순수 로직(테스트 대상).
 *
 * @param enabled 안내 허용 조건(로그인 + 메인 화면).
 * @param evaluatedThisSession 이번 실행에서 이미 평가했는지.
 * @param dismissedPermanently 사용자가 "나중에"로 영구 억제했는지.
 * @param permissionMissing 전체화면/오버레이 권한이 아직 없는지.
 */
internal fun shouldPromptAlarmDisplay(
    enabled: Boolean,
    evaluatedThisSession: Boolean,
    dismissedPermanently: Boolean,
    permissionMissing: Boolean
): Boolean =
    enabled && !evaluatedThisSession && !dismissedPermanently && permissionMissing

private fun Context.needsAlarmDisplayPermission(): Boolean {
    val fullScreenMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() == false
    val overlayMissing = !Settings.canDrawOverlays(this)
    return fullScreenMissing || overlayMissing
}

/** 미허용된 권한 화면으로 이동(전체 화면 알림 → 다른 앱 위에 표시 순서, 한 번에 하나씩). */
private fun Context.openAlarmDisplaySettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager != null && !notificationManager.canUseFullScreenIntent()) {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                        .setData(Uri.fromParts("package", packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            return
        }
    }
    if (!Settings.canDrawOverlays(this)) {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .setData(Uri.fromParts("package", packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private fun Context.isAlarmDisplayPrompted(): Boolean =
    getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_ALARM_DISPLAY_PROMPTED, false)

private fun Context.markAlarmDisplayPrompted() {
    getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ALARM_DISPLAY_PROMPTED, true)
        .apply()
}

private const val PROMPT_PREFS = "wakepoint_prompts"
private const val KEY_ALARM_DISPLAY_PROMPTED = "alarm_display_prompted"

private fun Context.missingStartupPermissions(): List<String> {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
    }
    return permissions.filterNot { hasPermission(it) }
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
