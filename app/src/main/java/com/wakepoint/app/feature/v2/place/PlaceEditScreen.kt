package com.wakepoint.app.feature.v2.place

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
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
import com.wakepoint.app.core.design.MapMarkerPreview
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMapBase
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSizeV2
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme

/**
 * v2 STEP2 (2b/2c) — 장소 등록·편집.
 * 상단 지도 프리뷰 · 내가 보는 이름 · 주소(자물쇠, 항상 비공개) · 친구에게 공개 토글.
 * 공개 ON: 연파랑 패널 전개(친구가 보는 이름[필수] + 방패 안내 + 친구 화면 미리보기).
 * 공개 OFF: 공유 이름/미리보기 숨김. 주소 필드는 두 상태 모두 잠금.
 */
@Composable
fun PlaceEditScreen(
    modifier: Modifier = Modifier,
    initialMyName: String = "",
    address: String = "서울 ○○구 ○○로 (나만 볼 수 있어요)",
    initialPublic: Boolean = false,
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    var myName by remember { mutableStateOf(initialMyName) }
    var isPublic by remember { mutableStateOf(initialPublic) }
    var sharedName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xl),
    ) {
        // 지도 프리뷰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WakepointSizeV2.mapPreviewHeight)
                .clip(WakepointShape.card)
                .background(WakepointMapBase),
            contentAlignment = Alignment.Center,
        ) {
            MapMarkerPreview()
        }

        Field(label = "내가 보는 이름") {
            WakepointTextField(value = myName, onValueChange = { myName = it }, placeholder = "예) 집, 여자친구집")
        }

        Field(label = "주소") {
            WakepointTextField(
                value = address,
                onValueChange = {},
                placeholder = "주소",
                readOnly = true,
                leadingIcon = Icons.Rounded.Lock,
            )
        }

        // 친구에게 공개 토글
        Row(
            modifier = Modifier.fillMaxWidth().height(WakepointSize.rowHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("친구에게 공개", style = MaterialTheme.typography.titleSmall, color = WakepointInk)
                Text("주소는 공개되지 않아요", style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
            }
            WakepointSwitch(checked = isPublic, onCheckedChange = { isPublic = it })
        }

        // 공개 ON 전개 패널 (연파랑)
        if (isPublic) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WakepointShape.card)
                    .background(WakepointPrimaryLight)
                    .padding(WakepointSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
            ) {
                Field(label = "친구가 보는 이름 *") {
                    WakepointTextField(value = sharedName, onValueChange = { sharedName = it }, placeholder = "예) 장소 2")
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        "친구는 이 이름만 볼 수 있고, 실제 주소와 위치는 알 수 없어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointTextSecondary,
                    )
                }
                // 친구 화면 미리보기 카드
                FriendPreviewCard(sharedName = sharedName.ifBlank { "장소 2" })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
        ) {
            WakepointDangerButton("삭제", Modifier.weight(1f), onClick = onDelete)
            WakepointButton("저장", Modifier.weight(1f), enabled = myName.isNotBlank() && (!isPublic || sharedName.isNotBlank()), onClick = onSave)
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        val danger = label.endsWith("*")
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = if (danger) WakepointInk else WakepointInk,
        )
        content()
    }
}

@Composable
private fun FriendPreviewCard(sharedName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(WakepointCanvas)
            .padding(WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(sharedName, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
            Text("사장님나빠여님의 장소", style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
        }
        Text("알람 보내기", style = MaterialTheme.typography.labelLarge, color = WakepointPrimary)
    }
}

@Preview(showBackground = true, widthDp = 390, name = "공개 OFF")
@Composable
private fun PlaceEditOffPreview() {
    WakepointTheme { PlaceEditScreen(initialMyName = "집") }
}

@Preview(showBackground = true, widthDp = 390, name = "공개 ON")
@Composable
private fun PlaceEditOnPreview() {
    WakepointTheme { PlaceEditScreen(initialMyName = "여자친구집", initialPublic = true) }
}
