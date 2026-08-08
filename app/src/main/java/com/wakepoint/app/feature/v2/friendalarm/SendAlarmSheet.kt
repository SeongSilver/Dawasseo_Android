package com.wakepoint.app.feature.v2.friendalarm

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointChipActiveBorder
import com.wakepoint.app.core.design.WakepointChipBorder
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTintButton
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight

private enum class SendTab(val label: String) { ADDRESS("주소"), PLACE("장소"), TIME("시간") }
private val RADIUS_OPTIONS = listOf("100m", "300m", "500m", "1km")
private val TIME_PRESETS = listOf("1시간 뒤", "오늘 밤 11시", "내일 오전 8시", "직접 선택")

/**
 * v2 STEP3 (3b~3e) — 친구에게 알람 보내기 시트(3탭).
 * 주소/장소/시간. 하단 공통 안내 "위치나 도착 여부는 볼 수 없어요."
 * 버전 가드(3e): 수신자 구버전이면 장소/시간 탭 비활성 + Warning 안내, 주소 탭만 지원.
 */
@Composable
fun SendAlarmSheet(
    recipientName: String,
    modifier: Modifier = Modifier,
    publicPlaces: List<String> = emptyList(),
    recipientOldVersion: Boolean = false,
    onSend: () -> Unit = {},
) {
    var tab by remember { mutableStateOf(SendTab.ADDRESS) }

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
        BottomSheetTitle(title = "${recipientName}님에게 알람 보내기")
        Spacer(Modifier.height(WakepointSpacing.lg))

        SegmentedTabs(selected = tab, onSelect = { tab = it }, disabledTabs = if (recipientOldVersion) setOf(SendTab.PLACE, SendTab.TIME) else emptySet())

        if (recipientOldVersion && tab == SendTab.ADDRESS) {
            Spacer(Modifier.height(WakepointSpacing.md))
            WarningPanel("상대가 구버전 앱을 쓰고 있어 주소 알람만 보낼 수 있어요.")
        }

        Spacer(Modifier.height(WakepointSpacing.xl))
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg),
        ) {
            when (tab) {
                SendTab.ADDRESS -> AddressTab()
                SendTab.PLACE -> PlaceTab(recipientName, publicPlaces)
                SendTab.TIME -> TimeTab()
            }

            InfoNote("위치나 도착 여부는 볼 수 없어요.")

            val sendEnabled = !(tab == SendTab.PLACE && publicPlaces.isEmpty())
            WakepointButton("보내기", Modifier.fillMaxWidth(), enabled = sendEnabled, onClick = onSend)
        }
    }
}

@Composable
private fun SegmentedTabs(selected: SendTab, onSelect: (SendTab) -> Unit, disabledTabs: Set<SendTab>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(WakepointField)
            .padding(WakepointSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
    ) {
        SendTab.entries.forEach { t ->
            val isSelected = t == selected
            val disabled = t in disabledTabs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(WakepointSize.chipHeight)
                    .clip(WakepointShape.control)
                    .background(if (isSelected) WakepointCanvas else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable(enabled = !disabled) { onSelect(t) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    t.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        disabled -> WakepointTextTertiary
                        isSelected -> WakepointPrimary
                        else -> WakepointTextSecondary
                    },
                )
            }
        }
    }
}

@Composable
private fun AddressTab() {
    var query by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(RADIUS_OPTIONS[1]) }
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }

    Field("주소 검색") {
        WakepointTextField(value = query, onValueChange = { query = it }, placeholder = "장소·주소 검색", leadingIcon = Icons.Rounded.Search)
    }
    Field("알람 이름") {
        WakepointTextField(value = name, onValueChange = { name = it }, placeholder = "예) 강남역 도착")
    }
    Field("알람 반경") {
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            RADIUS_OPTIONS.forEach { r ->
                Chip(r, radius == r, Modifier.weight(1f)) { radius = r }
            }
        }
    }
    ToggleRow("소리", soundOn) { soundOn = it }
    ToggleRow("진동", vibrationOn) { vibrationOn = it }
}

@Composable
private fun PlaceTab(recipientName: String, publicPlaces: List<String>) {
    if (publicPlaces.isEmpty()) {
        EmptyState(
            message = "$recipientName 님이 아직 공개한 장소가 없어요",
            actionText = "등록 요청 보내기",
        )
    } else {
        var selected by remember { mutableStateOf(publicPlaces.first()) }
        Field("$recipientName 님의 공개 장소") {
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                publicPlaces.forEach { place ->
                    SoundOptionRow(
                        title = place,
                        icon = Icons.Rounded.LocationOn,
                        selected = selected == place,
                        onClick = { selected = place },
                    )
                }
            }
        }
        InfoNote("위치는 $recipientName 님만 알고 있어요. 주소·반경은 상대 설정을 따라요.")
    }
}

@Composable
private fun TimeTab() {
    var preset by remember { mutableStateOf(TIME_PRESETS.first()) }
    var name by remember { mutableStateOf("") }
    Field("언제 울릴까요?") {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                TIME_PRESETS.take(2).forEach { p -> Chip(p, preset == p, Modifier.weight(1f)) { preset = p } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                TIME_PRESETS.drop(2).forEach { p -> Chip(p, preset == p, Modifier.weight(1f)) { preset = p } }
            }
        }
    }
    InfoNote("한 번만 울려요. 반복은 상대가 설정할 수 있어요.")
    Field("알람 이름") {
        WakepointTextField(value = name, onValueChange = { name = it }, placeholder = "예) 약 먹을 시간")
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        content()
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(WakepointShape.control)
            .background(if (selected) WakepointPrimaryLight else WakepointCanvas)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) WakepointChipActiveBorder else WakepointChipBorder,
                shape = WakepointShape.control,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (selected) WakepointPrimary else WakepointTextSecondary)
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(WakepointSize.rowHeight), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = WakepointInk, modifier = Modifier.weight(1f))
        com.wakepoint.app.core.design.WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.height(20.dp).then(Modifier))
        Text(text, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
    }
}

@Composable
private fun WarningPanel(text: String) {
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
        Text(text, style = MaterialTheme.typography.bodySmall, color = WakepointWarning)
    }
}

@Composable
private fun EmptyState(message: String, actionText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = WakepointSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = WakepointTextTertiary)
        WakepointTintButton(actionText)
    }
}

@Preview(showBackground = true, widthDp = 390, name = "주소 탭")
@Composable
private fun SendAlarmAddressPreview() {
    WakepointTheme { SendAlarmSheet(recipientName = "민수") }
}

@Preview(showBackground = true, widthDp = 390, name = "장소 탭 - 공개 장소 있음")
@Composable
private fun SendAlarmPlacePreview() {
    WakepointTheme { SendAlarmSheet(recipientName = "민수", publicPlaces = listOf("집", "회사")) }
}

@Preview(showBackground = true, widthDp = 390, name = "버전 가드")
@Composable
private fun SendAlarmGuardPreview() {
    WakepointTheme { SendAlarmSheet(recipientName = "민수", recipientOldVersion = true) }
}
