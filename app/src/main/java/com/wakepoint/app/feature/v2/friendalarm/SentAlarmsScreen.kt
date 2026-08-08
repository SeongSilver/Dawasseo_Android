package com.wakepoint.app.feature.v2.friendalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointPositive
import com.wakepoint.app.core.design.WakepointPositiveLight
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.feature.v2.model.SentAlarmStatus

/** 보낸 알람 UI 모델. */
data class V2SentAlarm(
    val recipient: String,
    val name: String,
    val meta: String,
    val status: SentAlarmStatus,
)

/**
 * v2 STEP3 (3f) — 보낸 알람 목록.
 * 상태 2단계(전송됨 → 기기 등록됨)까지만 발신자에게 노출. 이후(끔/도착)는 상호 비가시.
 * 항목별 [취소](Danger 텍스트).
 */
@Composable
fun SentAlarmsScreen(
    items: List<V2SentAlarm>,
    modifier: Modifier = Modifier,
    onCancel: (V2SentAlarm) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground),
    ) {
        WakepointPageHeader(title = "보낸 알람", subtitle = "기기 등록까지만 보여요. 이후는 친구의 알람이에요.")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap),
        ) {
            items.forEach { SentAlarmCard(it, onCancel = { onCancel(it) }) }
        }
    }
}

@Composable
private fun SentAlarmCard(item: V2SentAlarm, onCancel: () -> Unit) {
    WakepointCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${item.recipient}님 · ${item.name}", style = MaterialTheme.typography.titleMedium, color = WakepointInk)
                Spacer(Modifier.height(WakepointSpacing.xs))
                Text(item.meta, style = MaterialTheme.typography.bodySmall, color = WakepointTextTertiary)
            }
            StatusBadge(item.status)
        }
        Spacer(Modifier.height(WakepointSpacing.md))
        Text(
            "취소",
            style = MaterialTheme.typography.labelLarge,
            color = WakepointDanger,
            modifier = Modifier.clickable(onClick = onCancel),
        )
    }
}

@Composable
private fun StatusBadge(status: SentAlarmStatus) {
    when (status) {
        SentAlarmStatus.SENT -> StatusPill(status.label, color = WakepointTextSecondary)
        SentAlarmStatus.REGISTERED -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = WakepointPositive, modifier = Modifier.height(16.dp))
            StatusPill(status.label, color = WakepointPositive, background = WakepointPositiveLight)
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SentAlarmsScreenPreview() {
    WakepointTheme {
        SentAlarmsScreen(
            items = listOf(
                V2SentAlarm("민수", "강남역 도착", "위치 · 300m", SentAlarmStatus.SENT),
                V2SentAlarm("지수", "약 먹을 시간", "시간 · 오늘 밤 11시", SentAlarmStatus.REGISTERED),
            ),
        )
    }
}
