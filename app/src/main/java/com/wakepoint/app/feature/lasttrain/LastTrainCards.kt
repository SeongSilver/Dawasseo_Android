package com.wakepoint.app.feature.lasttrain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDarkTile
import com.wakepoint.app.core.design.WakepointImminentRing
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSizeV2
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTypeV2
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight
import com.wakepoint.app.feature.timealarm.TimeAlarmFormat

/**
 * v2 STEP4 (4-1) — 막차 메인 카드(여유/임박/아슬 3단계).
 * 위계: 남은 시간(hero, 52/Bold) › 데드라인 › 병목 근거. 단계별 색/링/주 액션 전환.
 */
@Composable
fun LastTrainCard(
    state: LastTrainResult.Available,
    modifier: Modifier = Modifier,
    onSetDeadlineAlarm: () -> Unit = {},
    onOpenRoute: () -> Unit = {},
    onTaxiLookup: () -> Unit = {},
) {
    val critical = state.level == LastTrainLevel.CRITICAL
    val containerColor = if (critical) WakepointDanger else WakepointCanvas
    val onContainer = if (critical) Color.White else WakepointInk
    val secondaryColor = if (critical) Color.White.copy(alpha = 0.85f) else WakepointTextSecondary
    val deadlineLabel = "${state.stationName} ${TimeAlarmFormat.timeText(state.deadlineHour, state.deadlineMinute)}"

    val ringModifier = when (state.level) {
        LastTrainLevel.IMMINENT -> Modifier.border(1.5.dp, WakepointImminentRing, WakepointShape.card)
        else -> Modifier
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(containerColor)
            .then(ringModifier)
            .padding(WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
    ) {
        LevelBadge(state.level)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WakepointSizeV2.lastTrainHeroMinHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                LastTrainPolicy.remainingText(state.remainingMinutes),
                style = WakepointTypeV2.lastTrainCountdown,
                color = onContainer,
            )
        }
        Text(deadlineLabel, style = MaterialTheme.typography.titleSmall, color = onContainer)
        Text(state.bottleneck, style = MaterialTheme.typography.bodySmall, color = secondaryColor)

        TaxiFareRow(fare = state.taxiFare, critical = critical, onLookup = onTaxiLookup)

        Spacer(Modifier.height(WakepointSpacing.xs))

        when (state.level) {
            LastTrainLevel.RELAXED -> WakepointButton(
                "${deadlineLabel}에 알람 걸기",
                Modifier.fillMaxWidth(),
                onClick = onSetDeadlineAlarm,
            )
            LastTrainLevel.IMMINENT -> WakepointButton(
                "지금 알람 걸기",
                Modifier.fillMaxWidth(),
                onClick = onSetDeadlineAlarm,
            )
            LastTrainLevel.CRITICAL -> {
                Text("알람은 이미 늦었어요", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Spacer(Modifier.height(WakepointSpacing.xs))
                DarkButton("경로 보기", onClick = onOpenRoute)
            }
        }
    }
}

@Composable
private fun LevelBadge(level: LastTrainLevel) {
    when (level) {
        LastTrainLevel.RELAXED -> StatusPill("여유", color = WakepointPrimary, background = WakepointPrimaryLight)
        LastTrainLevel.IMMINENT -> StatusPill("임박", color = WakepointWarning, background = WakepointWarningLight)
        LastTrainLevel.CRITICAL -> StatusPill("아슬", color = WakepointDanger, background = Color.White)
    }
}

@Composable
private fun TaxiFareRow(fare: String?, critical: Boolean, onLookup: () -> Unit) {
    if (fare == null) {
        Text(
            "택시비 탭해서 조회",
            style = MaterialTheme.typography.bodyMedium,
            color = if (critical) Color.White else WakepointPrimary,
            modifier = Modifier.clickable(onClick = onLookup),
        )
    } else {
        Text(
            fare,
            style = WakepointTypeV2.amountEmphasis,
            color = if (critical) Color.White else WakepointInk,
        )
    }
}

@Composable
private fun DarkButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.buttonHeight)
            .clip(WakepointShape.control)
            .background(WakepointDarkTile)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/**
 * v2 STEP4 (4-2) — 막차 끊김. 첫차 시각 + 택시비(금액 강조) +
 * [카카오맵에서 보기] + [첫차 …에 알람 걸기].
 */
@Composable
fun LastTrainEndedCard(
    state: LastTrainResult.Ended,
    modifier: Modifier = Modifier,
    onOpenKakao: () -> Unit = {},
    onSetFirstTrainAlarm: () -> Unit = {},
) {
    val firstTrainLabel = TimeAlarmFormat.timeText(state.firstTrainHour, state.firstTrainMinute)
    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text("오늘은 대중교통이 끊겼어요", style = MaterialTheme.typography.titleLarge, color = WakepointInk)
            Text("첫차 $firstTrainLabel", style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
            Text(state.taxiFare, style = WakepointTypeV2.amountEmphasis, color = WakepointInk)
            Text(state.taxiDetail, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
            Spacer(Modifier.height(WakepointSpacing.xs))
            WakepointSecondaryButton("카카오맵에서 보기", Modifier.fillMaxWidth(), icon = Icons.Rounded.Map, onClick = onOpenKakao)
            WakepointButton("첫차 ${firstTrainLabel}에 알람 걸기", Modifier.fillMaxWidth(), onClick = onSetFirstTrainAlarm)
        }
    }
}

/** v2 STEP4 (4-3) — 집 미등록 유도. */
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(16.dp))
                Text("주소는 나만 볼 수 있어요.", style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
            }
        }
    }
}

/** v2 STEP4 (4-4) — 미지원 구간 안내. */
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
