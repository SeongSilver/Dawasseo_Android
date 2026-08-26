package com.wakepoint.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.domain.model.Alarm

/**
 * v2 STEP3 (3-7) — 알람 수신 다이얼로그.
 * "OO님이 알람을 보냈어요" + 알람 스펙(종류/이름/위치/반경/소리/진동) + [거절][활성화].
 * 거절 = 알람 삭제, 활성화 = 유지(수신 알람은 활성 상태로 도착).
 */
@Composable
fun ReceiveAlarmDialog(
    alarm: Alarm,
    senderName: String,
    onActivate: () -> Unit,
    onReject: () -> Unit,
) {
    val radiusLabel = if (alarm.radiusKm < 1.0) {
        "${(alarm.radiusKm * 1000).toInt()}m"
    } else {
        "${alarm.radiusKm.toInt()}km"
    }
    Dialog(onDismissRequest = onActivate) {
        Surface(shape = WakepointShape.card, color = WakepointCanvas, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg),
            ) {
                Text(
                    "${senderName}님이 알람을 보냈어요",
                    style = MaterialTheme.typography.titleLarge,
                    color = WakepointInk,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WakepointShape.control)
                        .background(WakepointField)
                        .padding(WakepointSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
                ) {
                    SpecRow("종류", "위치 알람")
                    SpecRow("이름", alarm.label)
                    SpecRow("위치", alarm.targetAddress)
                    SpecRow("반경", radiusLabel)
                    SpecRow("소리", if (alarm.soundEnabled) "켜짐" else "꺼짐")
                    SpecRow("진동", if (alarm.vibrate) "켜짐" else "꺼짐")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
                ) {
                    WakepointSecondaryButton("거절", Modifier.weight(1f), onClick = onReject)
                    WakepointButton("활성화", Modifier.weight(1f), onClick = onActivate)
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = WakepointTextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, color = WakepointInk)
    }
}
