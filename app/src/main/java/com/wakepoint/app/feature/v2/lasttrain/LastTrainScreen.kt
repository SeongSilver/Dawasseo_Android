package com.wakepoint.app.feature.v2.lasttrain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDarkTile
import com.wakepoint.app.core.design.WakepointImminentRing
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSizeV2
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTypeV2
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight
import com.wakepoint.app.feature.v2.model.LastTrainLevel

/** 막차 상태 UI 모델. */
data class V2LastTrain(
    val remaining: String,
    val deadline: String,
    val bottleneck: String,
    val taxiFare: String?,
    val level: LastTrainLevel,
)

/**
 * v2 STEP4 (4a) — 막차 메인 카드(여유/임박/아슬 3단계).
 * 위계: 남은 시간(hero) › 데드라인 › 병목 근거. 단계별 색/링/주 액션 전환.
 */
@Composable
fun LastTrainCard(
    state: V2LastTrain,
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit = {},
    onTaxiLookup: () -> Unit = {},
) {
    val critical = state.level == LastTrainLevel.CRITICAL
    val containerColor = if (critical) WakepointDanger else WakepointCanvas
    val onContainer = if (critical) Color.White else WakepointInk
    val secondaryColor = if (critical) Color.White.copy(alpha = 0.85f) else WakepointTextSecondary

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
            modifier = Modifier.fillMaxWidth().heightIn(min = WakepointSizeV2.lastTrainHeroMinHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(state.remaining, style = WakepointTypeV2.lastTrainCountdown, color = onContainer)
        }
        Text(state.deadline, style = MaterialTheme.typography.titleSmall, color = onContainer)
        Text(state.bottleneck, style = MaterialTheme.typography.bodySmall, color = secondaryColor)

        // 택시비 — 여유 단계는 "탭해서 조회" placeholder
        TaxiFareRow(fare = state.taxiFare, critical = critical, onLookup = onTaxiLookup)

        Spacer(Modifier.height(WakepointSpacing.xs))

        when (state.level) {
            LastTrainLevel.RELAXED -> WakepointButton("${state.deadline}에 알람 걸기", Modifier.fillMaxWidth(), onClick = onPrimaryAction)
            LastTrainLevel.IMMINENT -> WakepointButton("지금 알람 걸기", Modifier.fillMaxWidth(), onClick = onPrimaryAction)
            LastTrainLevel.CRITICAL -> {
                Text("알람은 이미 늦었어요", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Spacer(Modifier.height(WakepointSpacing.xs))
                DarkButton("경로 보기", onClick = onPrimaryAction)
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
        Text(fare, style = WakepointTypeV2.amountEmphasis, color = if (critical) Color.White else WakepointInk)
    }
}

@Composable
private fun DarkButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(com.wakepoint.app.core.design.WakepointSize.buttonHeight)
            .clip(WakepointShape.control)
            .background(WakepointDarkTile)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/** 데모: 3단계 나열. */
@Composable
fun LastTrainDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap),
    ) {
        LastTrainCard(V2LastTrain("23분", "선릉역 23:02", "도보 8분 + 환승 대기", null, LastTrainLevel.RELAXED))
        LastTrainCard(V2LastTrain("9분", "선릉역 23:02", "지금 나가야 해요", "약 12,000원", LastTrainLevel.IMMINENT))
        LastTrainCard(V2LastTrain("-4분", "선릉역 23:02", "막차가 곧 끊겨요", "약 28,000원", LastTrainLevel.CRITICAL))
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun LastTrainDemoPreview() {
    WakepointTheme { LastTrainDemo() }
}
