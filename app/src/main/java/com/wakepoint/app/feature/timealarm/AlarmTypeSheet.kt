package com.wakepoint.app.feature.timealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTheme

/**
 * v2 STEP1 (1-1) — 알람 타입 선택 바텀시트 콘텐츠.
 * "어떤 알람을 만들까요?" · [위치 알람]/[시간 알람] 2택 + 취소.
 * 알람 탭의 + 버튼에서만 열린다(지도 탭 "알람 만들기"는 위치 알람 직행 — 이 시트 스킵).
 * ModalBottomSheet 안에서 쓰는 콘텐츠라 핸들은 그리지 않는다(호스트의 dragHandle 사용).
 */
@Composable
fun AlarmTypeSheetContent(
    onSelectLocation: () -> Unit,
    onSelectTime: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
    ) {
        BottomSheetTitle(title = "어떤 알람을 만들까요?")
        Spacer(Modifier.height(WakepointSpacing.lg))
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
            .background(LocalWakepointColors.current.background)
            .clickable(onClick = onClick)
            .padding(WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(WakepointSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(WakepointSize.circleButton)
                .clip(WakepointShape.message)
                .background(LocalWakepointColors.current.brandSubtle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = LocalWakepointColors.current.brand, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = LocalWakepointColors.current.text)
            Spacer(Modifier.height(WakepointSpacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LocalWakepointColors.current.textSecondary)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = LocalWakepointColors.current.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AlarmTypeSheetPreview() {
    WakepointTheme {
        Box(Modifier.background(LocalWakepointColors.current.surfaceElevated)) {
            AlarmTypeSheetContent(onSelectLocation = {}, onSelectTime = {}, onCancel = {})
        }
    }
}
