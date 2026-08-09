package com.wakepoint.app.feature.v2.timealarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDisabledText
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointStar
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.feature.v2.model.AlarmKind
import com.wakepoint.app.feature.v2.model.V2AlarmItem

/**
 * v2 STEP1 (1d) — 알람 목록 아이템 카드(타입 구분 배지).
 * 기존 목록 섹션(즐겨찾기/활성/지난)은 유지, 메타줄에 타입 아이콘+라벨(위치/시간) 노출.
 * 활성=Primary, 비활성=Disabled. 친구 설정 알람엔 person 아이콘 + "지수님이 설정" 배지.
 *
 * v1.5 세션이 편집 중인 AlarmsScreen.kt 를 건드리지 않도록 신규 데모 카드로 구현한다.
 */
@Composable
fun AlarmTypeBadgeCard(
    item: V2AlarmItem,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit = {},
) {
    val contentColor = if (item.past || !item.enabled) WakepointDisabledText else WakepointInk
    WakepointCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)) {
                    if (item.favorite) {
                        Icon(Icons.Rounded.Star, contentDescription = "즐겨찾기", tint = WakepointStar, modifier = Modifier.size(16.dp))
                    }
                    Text(item.name, style = MaterialTheme.typography.titleMedium, color = contentColor)
                }
                Spacer(Modifier.height(WakepointSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)) {
                    TypeLabel(item.kind, active = item.enabled && !item.past)
                    Text("·", style = MaterialTheme.typography.bodySmall, color = WakepointTextTertiary)
                    Text(item.meta, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
                }
                if (item.setByFriendName != null) {
                    Spacer(Modifier.height(WakepointSpacing.sm))
                    FriendSetByBadge(item.setByFriendName)
                }
            }
            if (!item.past) {
                WakepointSwitch(checked = item.enabled, onCheckedChange = onToggle)
            }
        }
    }
}

@Composable
private fun TypeLabel(kind: AlarmKind, active: Boolean) {
    val color = if (active) WakepointPrimary else WakepointDisabledText
    val icon = if (kind == AlarmKind.LOCATION) Icons.Rounded.LocationOn else Icons.Rounded.Schedule
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(kind.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun FriendSetByBadge(name: String) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(com.wakepoint.app.core.design.WakepointPrimaryLight)
            .padding(horizontal = WakepointSpacing.sm, vertical = WakepointSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
    ) {
        Icon(Icons.Rounded.Person, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(14.dp))
        Text("${name}님이 설정", style = MaterialTheme.typography.labelSmall, color = WakepointPrimary)
    }
}

/** 데모: 섹션(즐겨찾기/활성/지난) 구조로 타입 배지 카드를 나열. */
@Composable
fun AlarmListV2Demo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap),
    ) {
        DemoSection("즐겨찾기", listOf(
            V2AlarmItem("아침 기상", AlarmKind.TIME, "매일 오전 7:00", favorite = true),
        ))
        DemoSection("활성", listOf(
            V2AlarmItem("회사 도착", AlarmKind.LOCATION, "회사 · 300m"),
            V2AlarmItem("약속 알람", AlarmKind.TIME, "오늘 오후 6:30", setByFriendName = "지수"),
        ))
        DemoSection("지난 알람", listOf(
            V2AlarmItem("병원 예약", AlarmKind.TIME, "어제 오후 2:00", enabled = false, past = true),
        ))
    }
}

@Composable
private fun DemoSection(title: String, items: List<V2AlarmItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionTitleGap)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)) {
            items.forEach { AlarmTypeBadgeCard(it) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun AlarmListV2DemoPreview() {
    WakepointTheme { AlarmListV2Demo() }
}
