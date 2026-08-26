package com.wakepoint.app.feature.lasttrain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.feature.timealarm.TimeAlarmFormat

/**
 * v2 STEP4 (4-6, 보내기) — 친구에게 막차 알람.
 * '막차 챙기라'는 트리거만 전송하고 계산은 수신자 기기에서 —
 * "전달되지 않는 것" 3종(집주소/현재위치/막차시각)을 발신 화면에 명시(명세).
 * 전송 스키마(FCM 트리거)가 아직 없어 네비 미배선 — 서버 확보 시 친구 탭에 연결한다.
 */
@Composable
fun FriendLastTrainSendCard(
    recipientName: String,
    modifier: Modifier = Modifier,
    sendEnabled: Boolean = false,
    onSend: () -> Unit = {},
) {
    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text(
                "${recipientName}님에게 막차 알람 보내기",
                style = MaterialTheme.typography.titleLarge,
                color = WakepointInk,
            )
            Text(
                "'막차 챙기라'는 신호만 보내요. 막차·택시비 계산은 ${recipientName}님 휴대폰에서 해요.",
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointTextSecondary,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WakepointShape.control)
                    .background(WakepointField)
                    .padding(WakepointSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            ) {
                Text("전달되지 않는 것", style = MaterialTheme.typography.titleSmall, color = WakepointInk)
                NotSentRow("집 주소")
                NotSentRow("현재 위치")
                NotSentRow("막차 시각")
            }
            WakepointButton(
                "막차 알람 보내기",
                Modifier.fillMaxWidth(),
                enabled = sendEnabled,
                onClick = onSend,
            )
        }
    }
}

/**
 * v2 STEP4 (4-6, 수신) — 4-1과 동일 위계로 표시 + [닫기][알람 걸기] + 톤 정책 박스.
 * 재미 모드(택시비 ≈ 비교)는 켜짐 기본, 설정에서 끌 수 있음을 안내(명세 톤 정책).
 */
@Composable
fun FriendLastTrainReceive(
    state: LastTrainResult.Available,
    modifier: Modifier = Modifier,
    funModeOn: Boolean = true,
    funComparison: String = "치킨 1.5마리",
    onSetAlarm: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val deadlineLabel = "${state.stationName} ${TimeAlarmFormat.timeText(state.deadlineHour, state.deadlineMinute)}"
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
    ) {
        LastTrainCard(state = state, onSetDeadlineAlarm = onSetAlarm)
        if (funModeOn && state.taxiFare != null) {
            FunToneBox(fare = state.taxiFare, comparison = funComparison)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
        ) {
            WakepointSecondaryButton("닫기", Modifier.weight(1f), onClick = onClose)
            WakepointButton("${deadlineLabel}에 알람 걸기", Modifier.weight(1f), onClick = onSetAlarm)
        }
    }
}

@Composable
private fun NotSentRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
    ) {
        Icon(Icons.Rounded.Block, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
    }
}

@Composable
private fun FunToneBox(fare: String, comparison: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(WakepointPrimaryLight)
            .padding(WakepointSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
    ) {
        Icon(Icons.Rounded.EmojiFoodBeverage, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text("$fare ≈ $comparison", style = MaterialTheme.typography.titleSmall, color = WakepointPrimary)
            Text(
                "재미 모드예요. 마이 › 알림 설정에서 끌 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = WakepointTextSecondary,
            )
        }
    }
}
