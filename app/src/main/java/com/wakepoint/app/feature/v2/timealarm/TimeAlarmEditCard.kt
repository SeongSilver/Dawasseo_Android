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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
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
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointChipActiveBorder
import com.wakepoint.app.core.design.WakepointChipBorder
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme

/**
 * v2 STEP1 (1e) — 시간 알람 편집 확장 카드.
 * v1 확장 카드와 동일 골격(반경 칩 자리에 시각 필드 + 반복 칩).
 * 이름/시각/반복/사운드/진동 + [삭제][저장] (2분할 1:1, 저장=오른쪽 Primary, 삭제=왼쪽 Danger).
 */
@Composable
fun TimeAlarmEditCard(
    modifier: Modifier = Modifier,
    initialName: String = "아침 기상",
    timeText: String = "오전 7:00",
    initialRepeat: RepeatMode = RepeatMode.DAILY,
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    var name by remember { mutableStateOf(initialName) }
    var repeat by remember { mutableStateOf(initialRepeat) }
    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }

    WakepointCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)) {
            LabeledField("알람 이름") {
                WakepointTextField(value = name, onValueChange = { name = it }, placeholder = "알람 이름")
            }
            LabeledField("시각") {
                TimeField(timeText)
            }
            LabeledField("반복") {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    listOf(RepeatMode.NONE, RepeatMode.DAILY, RepeatMode.WEEKDAY, RepeatMode.WEEKEND).forEach { mode ->
                        EditChip(mode.label, repeat == mode, Modifier.weight(1f)) { repeat = mode }
                    }
                }
            }
            EditToggleRow("소리", soundOn) { soundOn = it }
            EditToggleRow("진동", vibrationOn) { vibrationOn = it }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            ) {
                WakepointDangerButton("삭제", Modifier.weight(1f), onClick = onDelete)
                WakepointButton("저장", Modifier.weight(1f), onClick = onSave)
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        content()
    }
}

@Composable
private fun TimeField(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.fieldHeight)
            .clip(WakepointShape.control)
            .background(WakepointField)
            .padding(horizontal = WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = WakepointInk, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EditChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(WakepointShape.control)
            .background(if (selected) WakepointPrimaryLight else WakepointBackground)
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
private fun EditToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(WakepointSize.rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = WakepointInk, modifier = Modifier.weight(1f))
        WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun TimeAlarmEditCardPreview() {
    WakepointTheme {
        Box(Modifier.background(WakepointBackground).padding(WakepointSpacing.screenHorizontal)) {
            TimeAlarmEditCard()
        }
    }
}
