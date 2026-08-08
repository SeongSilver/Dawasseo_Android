package com.wakepoint.app.feature.v2.timealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight

/**
 * v2 STEP1 (1f) — 정확 알람 권한 안내 다이얼로그.
 * "정한 시각에 정확히 깨워드릴까요?" · [설정으로 이동][나중에].
 * 설치당 1회·비강제(v1 패턴). 미허용 시 목록에 [AccuracyLimitedBadge] 노출.
 */
@Composable
fun ExactAlarmPermissionDialog(
    onGoToSettings: () -> Unit = {},
    onLater: () -> Unit = {},
) {
    WakepointConfirmDialog(
        title = "정한 시각에 정확히 깨워드릴까요?",
        message = "권한을 허용하면 정한 시각에 정확히 울려요. 허용하지 않아도 몇 분 늦게 울릴 수 있어요.",
        confirmText = "설정으로 이동",
        cancelText = "나중에",
        onConfirm = onGoToSettings,
        onDismiss = onLater,
    )
}

/**
 * v2 STEP1 (1f) — "정확도 제한" 배지. Warning 계열(비강제 안내, Danger 아님).
 * 탭 시 안내 시트/토스트 + [설정으로 이동] 연결(호출측 처리).
 */
@Composable
fun AccuracyLimitedBadge(modifier: Modifier = Modifier) {
    StatusPill(
        text = "정확도 제한",
        color = WakepointWarning,
        background = WakepointWarningLight,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ExactAlarmPermissionDialogPreview() {
    WakepointTheme { ExactAlarmPermissionDialog() }
}

@Preview(showBackground = true)
@Composable
private fun AccuracyLimitedBadgePreview() {
    WakepointTheme {
        Box(Modifier.background(WakepointBackground).padding(WakepointSpacing.lg)) {
            AccuracyLimitedBadge()
        }
    }
}
