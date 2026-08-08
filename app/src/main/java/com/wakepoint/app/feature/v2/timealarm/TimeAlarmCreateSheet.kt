package com.wakepoint.app.feature.v2.timealarm

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointChipActiveBorder
import com.wakepoint.app.core.design.WakepointChipBorder
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextDisabled
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme

private val WEEKDAY_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")

/**
 * v2 STEP1 (1b/1c) — 시간 알람 생성 바텀시트.
 * 이름 / 날짜·시각(기본 "다음 정시") / 반복 / 소리·벨소리 / 진동 / [취소][알람 추가].
 * 반복 "요일 직접" 선택 시 요일 원형 칩 노출 + 날짜 잠김, 과거 시각 시 오류 상태.
 *
 * @param timeIsPast 선택된 시각이 과거인지(실제 판정은 뷰모델/피커. 피커 미명세 → 파라미터로 주입).
 */
@Composable
fun TimeAlarmCreateSheet(
    modifier: Modifier = Modifier,
    initialRepeat: RepeatMode = RepeatMode.NONE,
    timeIsPast: Boolean = false,
    dateText: String = "오늘",
    timeText: String = "다음 정시",
    onCancel: () -> Unit = {},
    onAdd: () -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf(initialRepeat) }
    var customDays by remember { mutableStateOf(emptySet<Int>()) }
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }

    val dateLocked = TimeAlarmLogic.isDateLocked(repeat)
    val addEnabled = TimeAlarmLogic.isAddEnabled(timeIsPast, repeat, customDays)

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
        BottomSheetTitle(title = "시간 알람")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xl),
        ) {
            Spacer(Modifier.height(WakepointSpacing.xs))

            Section(label = "알람 이름") {
                WakepointTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "예) 아침 기상",
                )
            }

            Section(label = "날짜 · 시각") {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    SelectBox(
                        text = if (dateLocked) "반복 사용 중" else dateText,
                        enabled = !dateLocked,
                        modifier = Modifier.weight(1f),
                    )
                    SelectBox(
                        text = timeText,
                        isError = timeIsPast,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (timeIsPast) {
                    Spacer(Modifier.height(WakepointSpacing.labelFieldGap))
                    Text(
                        text = "이미 지난 시각이에요",
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointDanger,
                    )
                }
            }

            Section(label = "반복") {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    RepeatMode.entries.take(3).forEach { mode ->
                        RepeatChip(mode.label, repeat == mode, Modifier.weight(1f)) { repeat = mode }
                    }
                }
                Spacer(Modifier.height(WakepointSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    RepeatMode.entries.drop(3).forEach { mode ->
                        RepeatChip(mode.label, repeat == mode, Modifier.weight(1f)) { repeat = mode }
                    }
                    Spacer(Modifier.weight(1f))
                }
                if (repeat == RepeatMode.CUSTOM) {
                    Spacer(Modifier.height(WakepointSpacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
                    ) {
                        WEEKDAY_LABELS.forEachIndexed { index, label ->
                            DayCircle(
                                label = label,
                                selected = index in customDays,
                                modifier = Modifier.weight(1f),
                            ) {
                                customDays = customDays.toMutableSet().apply {
                                    if (!add(index)) remove(index)
                                }
                            }
                        }
                    }
                }
            }

            ToggleRow("소리", soundOn) { soundOn = it }
            if (soundOn) {
                SelectRow(icon = Icons.Rounded.Notifications, title = "벨소리", value = "기본 벨소리")
            }
            ToggleRow("진동", vibrationOn) { vibrationOn = it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            ) {
                WakepointSecondaryButton("취소", Modifier.weight(1f), onClick = onCancel)
                WakepointButton("알람 추가", Modifier.weight(1f), enabled = addEnabled, onClick = onAdd)
            }
        }
    }
}

@Composable
private fun Section(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        content()
    }
}

@Composable
private fun SelectBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val borderColor = when {
        isError -> WakepointDanger
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .height(WakepointSize.fieldHeight)
            .clip(WakepointShape.control)
            .background(WakepointField)
            .border(1.5.dp, borderColor, WakepointShape.control)
            .padding(horizontal = WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) WakepointInk else WakepointTextDisabled,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Rounded.ExpandMore,
            contentDescription = null,
            tint = if (enabled) WakepointIconMuted else WakepointTextDisabled,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun RepeatChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) WakepointPrimary else WakepointTextSecondary,
        )
    }
}

@Composable
private fun DayCircle(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(CircleShape)
            .background(if (selected) WakepointPrimaryLight else WakepointCanvas)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) WakepointChipActiveBorder else WakepointChipBorder,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) WakepointPrimary else WakepointTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(WakepointSize.rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = WakepointInk, modifier = Modifier.weight(1f))
        com.wakepoint.app.core.design.WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SelectRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight)
            .clip(WakepointShape.control)
            .background(WakepointField)
            .padding(horizontal = WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
    ) {
        Icon(icon, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = WakepointInk, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
        Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(20.dp))
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun TimeAlarmCreateSheetPreview() {
    WakepointTheme { TimeAlarmCreateSheet() }
}

@Preview(showBackground = true, widthDp = 390, name = "요일직접 + 과거시각 오류")
@Composable
private fun TimeAlarmCreateSheetErrorPreview() {
    WakepointTheme { TimeAlarmCreateSheet(initialRepeat = RepeatMode.CUSTOM, timeIsPast = true) }
}
