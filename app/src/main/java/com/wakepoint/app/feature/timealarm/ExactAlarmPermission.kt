package com.wakepoint.app.feature.timealarm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import com.wakepoint.app.core.design.WakepointConfirmDialog

/**
 * v2 STEP1 (1-5) — 정확 알람(SCHEDULE_EXACT_ALARM) 권한 안내 다이얼로그.
 * "정한 시각에 정확히 깨워드릴까요?" · [설정으로 이동][나중에].
 * 시간 알람 생성 시 미허용이면 설치당 1회만 안내(비강제, v1 전체화면 권한 안내와 같은 패턴).
 * 미허용 상태로 두면 목록 카드에 [AccuracyLimitedBadge] 가 떠서 언제든 설정으로 갈 수 있다.
 */
@Composable
fun ExactAlarmPermissionDialog(
    onGoToSettings: () -> Unit,
    onLater: () -> Unit,
) {
    WakepointConfirmDialog(
        title = "정한 시각에 정확히 깨워드릴까요?",
        message = "권한을 허용하면 정한 시각에 정확히 울려요. 허용하지 않아도 알람은 울리지만 몇 분 늦을 수 있어요.",
        confirmText = "설정으로 이동",
        cancelText = "나중에",
        onConfirm = onGoToSettings,
        onDismiss = onLater,
    )
}

/**
 * 안내를 띄울지 결정하는 순수 로직(테스트 대상).
 * 정확 알람이 이미 허용됐거나, 사용자가 한 번 응답(설치당 1회)했으면 다시 묻지 않는다.
 */
internal fun shouldPromptExactAlarm(
    exactAlarmAllowed: Boolean,
    alreadyPrompted: Boolean,
): Boolean = !exactAlarmAllowed && !alreadyPrompted

/** 시스템 "알람 및 리마인더" 설정 화면으로 이동(API 31+ 전용 권한이라 이하 버전은 no-op). */
fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    runCatching {
        startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData(Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

fun Context.isExactAlarmPrompted(): Boolean =
    getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_EXACT_ALARM_PROMPTED, false)

fun Context.markExactAlarmPrompted() {
    getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_EXACT_ALARM_PROMPTED, true)
        .apply()
}

/** v1 권한 안내와 같은 prefs 파일을 공유한다(core/permission/StartupPermissions.kt). */
private const val PROMPT_PREFS = "wakepoint_prompts"
private const val KEY_EXACT_ALARM_PROMPTED = "exact_alarm_prompted"
