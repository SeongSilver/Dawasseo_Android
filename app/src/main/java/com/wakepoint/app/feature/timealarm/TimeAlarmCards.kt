package com.wakepoint.app.feature.timealarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.core.alarm.TimeAlarmSchedule
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.ZonedDateTime

/**
 * v2 STEP1 (1-3) — 알람 목록 타입 라벨(위치/시간). 메타줄 맨 앞에 아이콘+라벨로 표시해
 * 한 목록에 섞인 두 타입을 구분한다. 활성=Primary, 비활성=Disabled.
 */
@Composable
fun AlarmTypeLabel(isTimeAlarm: Boolean, active: Boolean) {
    val color = if (active) LocalWakepointColors.current.brand else LocalWakepointColors.current.disabledContent
    val icon = if (isTimeAlarm) Icons.Rounded.Schedule else Icons.Rounded.LocationOn
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            if (isTimeAlarm) "시간" else "위치",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

/** v2 STEP1 (1-6) — 정확 알람 미허용 안내 배지(Warning 톤, 비강제). */
@Composable
fun AccuracyLimitedBadge(modifier: Modifier = Modifier) {
    StatusPill(
        text = "정확도 제한",
        color = LocalWakepointColors.current.cautionary,
        background = LocalWakepointColors.current.cautionarySurface,
        modifier = modifier,
    )
}

/**
 * 시간 알람 목록 카드. 접힘(1-3: 이름/타입·메타/토글) ↔ 펼침(1-4: 편집 확장 카드).
 * 위치 알람 카드(AlarmsScreen)와 같은 골격을 유지한다.
 */
@Composable
fun TimeAlarmCard(
    alarm: TimeAlarm,
    expanded: Boolean,
    isUpdating: Boolean,
    exactAlarmAllowed: Boolean,
    onToggleExpanded: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onSave: (TimeAlarm) -> Unit,
    onDeleteRequest: () -> Unit,
    onAccuracyBadgeClick: () -> Unit,
) {
    if (expanded) {
        TimeAlarmEditCard(
            alarm = alarm,
            isUpdating = isUpdating,
            onToggleExpanded = onToggleExpanded,
            onEnabledChange = onEnabledChange,
            onSave = onSave,
            onDeleteRequest = onDeleteRequest,
        )
    } else {
        CompactTimeAlarmCard(
            alarm = alarm,
            isUpdating = isUpdating,
            exactAlarmAllowed = exactAlarmAllowed,
            onToggleExpanded = onToggleExpanded,
            onEnabledChange = onEnabledChange,
            onAccuracyBadgeClick = onAccuracyBadgeClick,
        )
    }
}

@Composable
private fun CompactTimeAlarmCard(
    alarm: TimeAlarm,
    isUpdating: Boolean,
    exactAlarmAllowed: Boolean,
    onToggleExpanded: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onAccuracyBadgeClick: () -> Unit,
) {
    WakepointCard(modifier = Modifier.clickable(onClick = onToggleExpanded)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
            ) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (alarm.enabled) LocalWakepointColors.current.text else LocalWakepointColors.current.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
                ) {
                    AlarmTypeLabel(isTimeAlarm = true, active = alarm.enabled)
                    Text("·", style = MaterialTheme.typography.bodySmall, color = LocalWakepointColors.current.textTertiary)
                    Text(
                        text = TimeAlarmFormat.metaText(alarm, ZonedDateTime.now()),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalWakepointColors.current.textSecondary,
                    )
                }
                if (alarm.enabled && !exactAlarmAllowed) {
                    AccuracyLimitedBadge(
                        modifier = Modifier.clickable(onClick = onAccuracyBadgeClick)
                    )
                }
            }
            WakepointSwitch(
                checked = alarm.enabled,
                onCheckedChange = onEnabledChange,
                enabled = !isUpdating,
            )
        }
    }
}

/**
 * v2 STEP1 (1-4) — 시간 알람 편집 확장 카드.
 * 이름/시각(피커)/반복(요일 직접 포함)/소리/진동 + [삭제][저장](2분할 1:1).
 */
@Composable
private fun TimeAlarmEditCard(
    alarm: TimeAlarm,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onSave: (TimeAlarm) -> Unit,
    onDeleteRequest: () -> Unit,
) {
    var name by remember(alarm.id, alarm.label) { mutableStateOf(alarm.label) }
    var hour by remember(alarm.id, alarm.hour) { mutableIntStateOf(alarm.hour) }
    var minute by remember(alarm.id, alarm.minute) { mutableIntStateOf(alarm.minute) }
    var repeat by remember(alarm.id, alarm.repeatMode) { mutableStateOf(alarm.repeatMode) }
    var customDays by remember(alarm.id, alarm.repeatDays) { mutableStateOf(alarm.repeatDays) }
    var soundOn by remember(alarm.id, alarm.soundEnabled) { mutableStateOf(alarm.soundEnabled) }
    var vibrationOn by remember(alarm.id, alarm.vibrate) { mutableStateOf(alarm.vibrate) }
    var showTimePicker by remember { mutableStateOf(false) }

    val saveEnabled = !isUpdating && TimeAlarmSchedule.isCreatable(repeat, customDays)

    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap)) {
            Row(
                modifier = Modifier.clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = alarm.label,
                    color = LocalWakepointColors.current.text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                WakepointSwitch(
                    checked = alarm.enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = !isUpdating,
                )
            }
            SheetSection(label = "알람 이름") {
                WakepointTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "예) 아침 기상",
                    readOnly = isUpdating,
                )
            }
            SheetSection(label = "시각") {
                SelectBox(
                    text = TimeAlarmFormat.timeText(hour, minute),
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SheetSection(label = "반복") {
                RepeatSelector(
                    repeat = repeat,
                    customDays = customDays,
                    onRepeatChange = { repeat = it },
                    onCustomDaysChange = { customDays = it },
                )
            }
            Column {
                SheetToggleRow("소리", soundOn) { soundOn = it }
                if (soundOn) {
                    RingtoneRow(value = "기본 벨소리")
                    Spacer(Modifier.height(WakepointSpacing.sm))
                }
                SheetToggleRow("진동", vibrationOn) { vibrationOn = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                WakepointDangerButton(
                    text = "삭제",
                    enabled = !isUpdating,
                    onClick = onDeleteRequest,
                    modifier = Modifier.weight(1f),
                )
                WakepointButton(
                    text = "저장",
                    enabled = saveEnabled,
                    onClick = {
                        onSave(
                            alarm.copy(
                                label = name.trim().ifBlank { alarm.label },
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
