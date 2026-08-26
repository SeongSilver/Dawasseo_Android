package com.wakepoint.app.core.design

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 반경 선택 세그먼트. v1.5 규칙: 항상 3열 그리드(반경 6종 → 3×2행), 칩 높이 44 ·
 * 라운드 12. 선택 시 PrimaryLight 배경 + Primary 테두리(1.5px), 미선택은 흰 배경 +
 * ChipBorder 테두리(1.5px)로 통일한다.
 */
@Composable
fun RadiusSelector(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
            ) {
                rowOptions.forEach { option ->
                    RadiusChip(
                        label = option,
                        selected = option == selectedOption,
                        onClick = { onSelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 마지막 행이 3개 미만이면 남는 칸을 비워 3열 정렬을 유지한다.
                repeat(3 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RadiusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(WakepointShape.control)
            .background(if (selected) WakepointPrimaryLight else WakepointCanvas)
            .border(
                width = 1.5.dp,
                color = if (selected) WakepointPrimary else WakepointChipBorder,
                shape = WakepointShape.control
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) WakepointPrimary else WakepointInk
        )
    }
}
