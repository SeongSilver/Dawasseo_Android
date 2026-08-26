package com.wakepoint.app.feature.timealarm

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.core.alarm.TimeAlarmSchedule
import com.wakepoint.app.core.design.BottomSheetTitle
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
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextDisabled
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.util.UUID

/**
 * v2 STEP1 (1-2) — 시간 알람 생성 바텀시트 콘텐츠.
 * 이름 / 날짜·시각(기본 "다음 정시") / 반복(요일 직접 시 원형 칩) / 소리 / 진동 / [취소][알람 추가].
 * 반복 선택 시 날짜는 잠기고([TimeAlarmSchedule.isDateLocked]) 요일 직접인데 요일이 없으면
 * 추가 버튼이 비활성([TimeAlarmSchedule.isCreatable]).
 *
 * 날짜는 도메인에 저장되지 않는다(일회성은 "다음 hh:mm"으로 자동 계산) — 표시 전용으로
 * 오늘/내일을 계산해 보여주므로 과거 시각 오류 상태가 생기지 않는다.
 */
@Composable
fun TimeAlarmCreateSheetContent(
    onCancel: () -> Unit,
    onAdd: (TimeAlarm) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initial = remember { ZonedDateTime.now().plusHours(1) }
    var name by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf(0) }
    var repeat by remember { mutableStateOf(RepeatMode.NONE) }
    var customDays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateLocked = TimeAlarmSchedule.isDateLocked(repeat)
    val addEnabled = TimeAlarmSchedule.isCreatable(repeat, customDays)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap),
    ) {
        BottomSheetTitle(title = "시간 알람")

        SheetSection(label = "알람 이름") {
            WakepointTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "예) 아침 기상",
            )
        }

        SheetSection(label = "날짜 · 시각") {
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                SelectBox(
                    text = if (dateLocked) {
                        "반복 사용 중"
                    } else {
                        TimeAlarmFormat.oneShotDayText(hour, minute, ZonedDateTime.now())
                    },
                    enabled = !dateLocked,
                    modifier = Modifier.weight(1f),
                )
                SelectBox(
                    text = TimeAlarmFormat.timeText(hour, minute),
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SheetSection(label = "반복") {
            RepeatSelector(
                repeat = repeat,
                customDays = customDays,
                onRepeatChange = { repeat = it },
                onCustomDaysChange = { customDays = it },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            SheetToggleRow("소리", soundOn) { soundOn = it }
            if (soundOn) {
                RingtoneRow(value = "기본 벨소리")
            }
            SheetToggleRow("진동", vibrationOn) { vibrationOn = it }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
        ) {
            WakepointSecondaryButton("취소", Modifier.weight(1f), onClick = onCancel)
            WakepointButton(
                text = "알람 추가",
                enabled = addEnabled,
                onClick = {
                    onAdd(
                        TimeAlarm(
                            id = UUID.randomUUID().toString(),
                            label = name.trim().ifBlank { "시간 알람" },
                            hour = hour,
                            minute = minute,
                            repeatMode = repeat,
                            repeatDays = if (repeat == RepeatMode.CUSTOM) customDays else emptySet(),
                            soundEnabled = soundOn,
                            vibrate = vibrationOn,
                        )
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showTimePicker) {
        WakepointTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

/** 반복 프리셋 칩 2줄 + 요일 직접 선택 시 요일 원형 칩(월~일). 생성/편집 공용. */
@Composable
fun RepeatSelector(
    repeat: RepeatMode,
    customDays: Set<DayOfWeek>,
    onRepeatChange: (RepeatMode) -> Unit,
    onCustomDaysChange: (Set<DayOfWeek>) -> Unit,
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            RepeatMode.entries.take(3).forEach { mode ->
                RepeatChip(mode.label, repeat == mode, Modifier.weight(1f)) { onRepeatChange(mode) }
            }
        }
        Spacer(Modifier.height(WakepointSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            RepeatMode.entries.drop(3).forEach { mode ->
                RepeatChip(mode.label, repeat == mode, Modifier.weight(1f)) { onRepeatChange(mode) }
            }
            Spacer(Modifier.weight(1f))
        }
        if (repeat == RepeatMode.CUSTOM) {
            Spacer(Modifier.height(WakepointSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
            ) {
                TimeAlarmFormat.WEEK_ORDER.forEach { day ->
                    DayCircle(
                        label = TimeAlarmFormat.dayLabel(day),
                        selected = day in customDays,
                        modifier = Modifier.weight(1f),
                    ) {
                        onCustomDaysChange(
                            if (day in customDays) customDays - day else customDays + day
                        )
                    }
                }
            }
        }
    }
}

/** M3 TimePicker 를 앱 다이얼로그 스타일(카드 라운드·2분할 버튼)로 감싼 시각 선택. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakepointTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(WakepointShape.card)
                .background(WakepointCanvas)
                .padding(WakepointSpacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg),
        ) {
            Text(
                text = "시각 선택",
                style = MaterialTheme.typography.titleSmall,
                color = WakepointInk,
                modifier = Modifier.fillMaxWidth(),
            )
            TimePicker(state = state)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            ) {
                WakepointSecondaryButton("취소", Modifier.weight(1f), onClick = onDismiss)
                WakepointButton("확인", Modifier.weight(1f), onClick = { onConfirm(state.hour, state.minute) })
            }
        }
    }
}

@Composable
internal fun SheetSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        content()
    }
}

/** 시트 안 선택 박스(52·라운드 12·Field 배경). 클릭 시 피커, 잠기면 비활성 톤. */
@Composable
internal fun SelectBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .height(WakepointSize.fieldHeight)
            .clip(WakepointShape.control)
            .background(WakepointField)
            .then(
                if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
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
internal fun RepeatChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(WakepointShape.control)
            .background(if (selected) WakepointPrimaryLight else Color.Transparent)
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
            .background(if (selected) WakepointPrimaryLight else Color.Transparent)
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
internal fun SheetToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = WakepointInk,
            modifier = Modifier.weight(1f),
        )
        WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** 벨소리 표시 행(현재는 기본 벨소리 고정 — 커스텀 벨소리 연동은 후속). */
@Composable
internal fun RingtoneRow(value: String) {
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
        Icon(
            Icons.Rounded.Notifications,
            contentDescription = null,
            tint = WakepointIconMuted,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "벨소리",
            style = MaterialTheme.typography.bodyLarge,
            color = WakepointInk,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, color = WakepointTextSecondary)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun TimeAlarmCreateSheetPreview() {
    WakepointTheme {
        Box(Modifier.background(WakepointCanvas)) {
            TimeAlarmCreateSheetContent(onCancel = {}, onAdd = {})
        }
    }
}
