package com.wakepoint.app.feature.v2.lasttrain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTypeV2

/**
 * v2 STEP4 (4b) — 막차 끊김.
 * 첫차 시각 + 택시비(심야할증 포함, 금액 강조) + [카카오맵에서 보기] + [첫차 …에 알람 걸기].
 */
@Composable
fun LastTrainEndedCard(
    firstTrainTime: String,
    taxiFare: String,
    taxiDetail: String,
    modifier: Modifier = Modifier,
    onOpenKakao: () -> Unit = {},
    onSetFirstTrainAlarm: () -> Unit = {},
) {
    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text("오늘은 대중교통이 끊겼어요", style = MaterialTheme.typography.titleLarge, color = WakepointInk)
            Text("첫차 $firstTrainTime", style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
            Text(taxiFare, style = WakepointTypeV2.amountEmphasis, color = WakepointInk)
            Text(taxiDetail, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
            Spacer(Modifier.height(WakepointSpacing.xs))
            WakepointSecondaryButton("카카오맵에서 보기", Modifier.fillMaxWidth(), icon = Icons.Rounded.Map, onClick = onOpenKakao)
            WakepointButton("첫차 ${firstTrainTime}에 알람 걸기", Modifier.fillMaxWidth(), onClick = onSetFirstTrainAlarm)
        }
    }
}

/**
 * v2 STEP4 (4c) — 집 미등록 유도.
 */
@Composable
fun LastTrainNoHomeCard(
    modifier: Modifier = Modifier,
    onRegisterHome: () -> Unit = {},
) {
    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text("집을 등록하면 막차를 챙겨드릴게요", style = MaterialTheme.typography.titleLarge, color = WakepointInk)
            Text("집까지 가는 막차·택시비를 알려드려요.", style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
            WakepointButton("집 등록하기", Modifier.fillMaxWidth(), icon = Icons.Rounded.Home, onClick = onRegisterHome)
            LockNote("주소는 나만 볼 수 있어요.")
        }
    }
}

/**
 * v2 STEP4 (4d) — 미지원 구간.
 */
@Composable
fun LastTrainUnsupportedCard(
    reason: String,
    modifier: Modifier = Modifier,
    onOpenKakao: () -> Unit = {},
) {
    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text("이 구간은 막차 안내를 지원하지 않아요", style = MaterialTheme.typography.titleLarge, color = WakepointInk)
            Text(reason, style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
            WakepointSecondaryButton("카카오맵에서 보기", Modifier.fillMaxWidth(), icon = Icons.Rounded.Map, onClick = onOpenKakao)
        }
    }
}

@Composable
private fun LockNote(text: String) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
    ) {
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.height(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun LastTrainStatesPreview() {
    WakepointTheme {
        Column(
            modifier = Modifier
                .background(WakepointBackground)
                .verticalScroll(rememberScrollState())
                .padding(WakepointSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap),
        ) {
            LastTrainEndedCard("05:34", "약 28,000원", "심야할증 포함 · 약 25분")
            LastTrainNoHomeCard()
            LastTrainUnsupportedCard("이 구간은 버스만 운행해 막차 판정을 지원하지 않아요.")
        }
    }
}
