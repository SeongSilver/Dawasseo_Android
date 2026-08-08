package com.wakepoint.app.feature.v2.friendalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight

/** 수신 알람 스펙(표현 전용). */
data class V2ReceivedAlarm(
    val sender: String,
    val type: String,
    val name: String,
    val schedule: String,
    val sound: String,
    val vibration: String,
)

/**
 * v2 STEP3 (3g) — 알람 수신 다이얼로그.
 * "지훈님이 알람을 보냈어요" + 알람 전체 스펙 + [거절][활성화(기본 강조)].
 * "받은 알람 자동 활성" 꺼져 있으면 안내 노출.
 */
@Composable
fun ReceiveAlarmDialog(
    alarm: V2ReceivedAlarm,
    modifier: Modifier = Modifier,
    autoActivateOff: Boolean = true,
    onActivate: () -> Unit = {},
    onReject: () -> Unit = {},
) {
    Dialog(onDismissRequest = onReject) {
        Surface(shape = WakepointShape.card, color = WakepointCanvas, modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(WakepointSpacing.xl), verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)) {
                Text("${alarm.sender}님이 알람을 보냈어요", style = MaterialTheme.typography.titleLarge, color = WakepointInk)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WakepointShape.control)
                        .background(WakepointField)
                        .padding(WakepointSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
                ) {
                    SpecRow("종류", alarm.type)
                    SpecRow("이름", alarm.name)
                    SpecRow("일시", alarm.schedule)
                    SpecRow("소리", alarm.sound)
                    SpecRow("진동", alarm.vibration)
                }

                if (autoActivateOff) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(WakepointShape.control)
                            .background(WakepointWarningLight)
                            .padding(WakepointSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = WakepointWarning, modifier = Modifier.height(20.dp))
                        Text("받은 알람 자동 활성이 꺼져 있어요. 활성화해야 울려요.", style = MaterialTheme.typography.bodySmall, color = WakepointWarning)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = WakepointInk)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ReceiveAlarmDialogPreview() {
    WakepointTheme {
        ReceiveAlarmDialog(
            alarm = V2ReceivedAlarm(
                sender = "지훈",
                type = "시간 알람",
                name = "약 먹을 시간",
                schedule = "오늘 밤 11:00",
                sound = "기본 벨소리",
                vibration = "켜짐",
            ),
        )
    }
}
