package com.wakepoint.app.feature.v2.timealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointIconLight
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme

/**
 * v2 STEP1 (1a) — 알람 타입 선택 바텀시트.
 * "어떤 알람을 만들까요?" · [위치 알람]/[시간 알람] 2택 + 취소.
 * 알람 탭의 + 버튼에서만 열린다(지도 탭 "알람 만들기"는 위치 알람 직행 — 이 시트 스킵).
 */
@Composable
fun AlarmTypeSheet(
    modifier: Modifier = Modifier,
    onSelectLocation: () -> Unit = {},
    onSelectTime: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointCanvas)
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
    ) {
        Spacer(Modifier.height(WakepointSpacing.md))
        BottomSheetHandle()
        Spacer(Modifier.height(WakepointSpacing.lg))
        BottomSheetTitle(title = "어떤 알람을 만들까요?")
        Spacer(Modifier.height(WakepointSpacing.xl))
        AlarmTypeOption(
            icon = Icons.Rounded.LocationOn,
            title = "위치 알람",
            subtitle = "정한 장소에 도착하면 울려요",
            onClick = onSelectLocation,
        )
        Spacer(Modifier.height(WakepointSpacing.cardGap))
        AlarmTypeOption(
            icon = Icons.Rounded.Schedule,
            title = "시간 알람",
            subtitle = "정한 시각에 울려요",
            onClick = onSelectTime,
        )
        Spacer(Modifier.height(WakepointSpacing.lg))
        WakepointSecondaryButton(
            text = "취소",
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AlarmTypeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(WakepointBackground)
            .clickable(onClick = onClick)
            .padding(WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(WakepointSize.chipHeight)
                .clip(WakepointShape.control)
                .background(WakepointPrimaryLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = WakepointIconLight,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AlarmTypeSheetPreview() {
    WakepointTheme { AlarmTypeSheet() }
}
