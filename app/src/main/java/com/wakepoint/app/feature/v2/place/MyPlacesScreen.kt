package com.wakepoint.app.feature.v2.place

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
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
import com.wakepoint.app.core.design.WakepointChipBorder
import com.wakepoint.app.core.design.WakepointIconLight
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.feature.v2.model.V2Place

private const val PLACE_SLOTS = 4

/**
 * v2 STEP2 (2a) — 내 장소 목록 + 마이 진입점.
 * 슬롯 사용량 n/4. 공개=눈 아이콘, 비공개=자물쇠. 공개 장소는 친구가 보는 이름("장소 2")로 표시.
 */
@Composable
fun MyPlacesScreen(
    places: List<V2Place>,
    modifier: Modifier = Modifier,
    onAddPlace: () -> Unit = {},
    onPlaceClick: (V2Place) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground),
    ) {
        WakepointPageHeader(title = "내 장소", subtitle = "${places.size} / $PLACE_SLOTS")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap),
        ) {
            places.forEach { place ->
                PlaceRow(place, onClick = { onPlaceClick(place) })
            }
            repeat((PLACE_SLOTS - places.size).coerceAtLeast(0)) {
                AddPlaceRow(onClick = onAddPlace)
            }
        }
    }
}

@Composable
private fun PlaceRow(place: V2Place, onClick: () -> Unit) {
    WakepointCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Box(
                modifier = Modifier
                    .size(WakepointSize.chipHeight)
                    .clip(WakepointShape.control)
                    .background(WakepointPrimaryLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (place.isPublic) Icons.Rounded.Visibility else Icons.Rounded.Lock,
                    contentDescription = if (place.isPublic) "공개" else "비공개",
                    tint = WakepointPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    Text(place.myName, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
                    if (place.isFixed) {
                        StatusPill("고정", color = WakepointTextSecondary)
                    }
                }
                Spacer(Modifier.height(WakepointSpacing.xs))
                Text(
                    if (place.isPublic) "공개 중 · 친구에겐 '${place.sharedName ?: "장소"}'로 보여요" else "비공개 · 나만 볼 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextSecondary,
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = WakepointIconLight, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AddPlaceRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight)
            .clip(WakepointShape.card)
            .border(1.5.dp, WakepointChipBorder, WakepointShape.card)
            .clickable(onClick = onClick)
            .padding(horizontal = WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(20.dp))
        Text("장소 추가", style = MaterialTheme.typography.bodyLarge, color = WakepointTextTertiary)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun MyPlacesScreenPreview() {
    WakepointTheme {
        MyPlacesScreen(
            places = listOf(
                V2Place(myName = "집", isFixed = true, isPublic = false),
                V2Place(myName = "여자친구집", isPublic = true, sharedName = "장소 2"),
            ),
        )
    }
}
