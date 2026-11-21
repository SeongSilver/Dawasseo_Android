package com.wakepoint.app.feature.lasttrain

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointSpacing

/**
 * v2 STEP4 (4-5) — 위치 전송 동의(최초 1회) 시트 콘텐츠.
 * 전달 항목/시점/저장여부 3줄 명세 + 도착 알람 판정은 로컬(서버 미저장) 재확인.
 * ModalBottomSheet 호스트의 dragHandle 을 쓰므로 자체 핸들은 그리지 않는다.
 */
@Composable
fun LocationConsentSheetContent(
    onAgree: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
    ) {
        BottomSheetTitle(
            title = "현재 위치를 카카오맵에 전달할까요?",
            subtitle = "막차·택시비를 조회할 때만 사용해요.",
        )
        Spacer(Modifier.height(WakepointSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)) {
            ConsentRow(Icons.Rounded.Shield, "전달 항목", "현재 위치와 집 주소(막차·경로 계산용)")
            ConsentRow(Icons.Rounded.Schedule, "전달 시점", "'조회' 버튼을 누를 때만, 그 순간에")
            ConsentRow(Icons.Rounded.CheckCircle, "저장 여부", "다왔어 서버에 저장하지 않아요. 도착 알람 판정은 휴대폰 안에서만.")
        }

        Spacer(Modifier.height(WakepointSpacing.xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
        ) {
            WakepointSecondaryButton("다음에 할게요", Modifier.weight(1f), onClick = onLater)
            WakepointButton("동의하고 조회", Modifier.weight(1f), onClick = onAgree)
        }
    }
}

@Composable
private fun ConsentRow(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
        Icon(icon, contentDescription = null, tint = LocalWakepointColors.current.brand, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = LocalWakepointColors.current.text)
            Text(body, style = MaterialTheme.typography.bodySmall, color = LocalWakepointColors.current.textSecondary)
        }
    }
}

private const val LAST_TRAIN_PREFS = "wakepoint_last_train"
private const val KEY_LOCATION_CONSENT = "location_consent"

/** 위치 전송 동의 여부(최초 1회 시트, 동의 후 재노출 없음). */
fun Context.isLastTrainConsentGiven(): Boolean =
    getSharedPreferences(LAST_TRAIN_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_LOCATION_CONSENT, false)

fun Context.markLastTrainConsentGiven() {
    getSharedPreferences(LAST_TRAIN_PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_LOCATION_CONSENT, true).apply()
}
