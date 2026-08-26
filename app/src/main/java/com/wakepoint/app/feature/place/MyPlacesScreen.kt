package com.wakepoint.app.feature.place

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointChipBorder
import com.wakepoint.app.core.design.WakepointIconLight
import com.wakepoint.app.core.design.WakepointIconMuted
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.domain.model.Place

/**
 * v2 STEP2 (2-1) — 내 장소 목록. 마이 탭 진입점에서 열리는 서브 화면.
 * 집 고정 슬롯 최상단(미등록이면 등록 유도), 슬롯 사용량 n/4, 남은 슬롯은 [장소 추가] 행.
 * 공개=눈 아이콘, 비공개=자물쇠. 공개 장소는 친구가 보는 이름을 함께 표시.
 */
@Composable
fun MyPlacesScreen(
    onBack: () -> Unit,
    onAddPlace: (isHome: Boolean) -> Unit,
    onEditPlace: (Place) -> Unit,
    viewModel: MyPlacesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(WakepointCanvas)
                .padding(horizontal = WakepointSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로",
                    tint = WakepointInk,
                )
            }
            Text(
                text = "내 장소",
                style = MaterialTheme.typography.titleSmall,
                color = WakepointInk,
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(bottom = WakepointSpacing.xl),
        ) {
            WakepointPageHeader(
                title = "내 장소",
                subtitle = "${uiState.slotText} 슬롯 사용 중 · 주소는 친구에게 공개되지 않아요",
                modifier = Modifier.padding(horizontal = 0.dp),
            )
            Spacer(Modifier.height(WakepointSpacing.sectionTitleGap))
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)) {
                if (uiState.isLoading) {
                    WakepointLoadingPlaceholder()
                } else {
                    val home = uiState.homePlace
                    if (home != null) {
                        PlaceRow(place = home, onClick = { onEditPlace(home) })
                    } else {
                        AddSlotRow(
                            icon = Icons.Rounded.Home,
                            label = "집 등록 (막차 알람에 쓰여요)",
                            onClick = { onAddPlace(true) },
                        )
                    }
                    uiState.otherPlaces.forEach { place ->
                        PlaceRow(place = place, onClick = { onEditPlace(place) })
                    }
                    if (uiState.canAdd) {
                        AddSlotRow(
                            icon = Icons.Rounded.Add,
                            label = "장소 추가",
                            onClick = { onAddPlace(false) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(place: Place, onClick: () -> Unit) {
    WakepointCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(WakepointSize.chipHeight)
                    .clip(WakepointShape.control)
                    .background(WakepointPrimaryLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        place.isHome -> Icons.Rounded.Home
                        place.isPublic -> Icons.Rounded.Visibility
                        else -> Icons.Rounded.Lock
                    },
                    contentDescription = if (place.isPublic) "공개" else "비공개",
                    tint = WakepointPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
                ) {
                    Text(place.myName, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
                    if (place.isHome) {
                        StatusPill("고정", color = WakepointTextSecondary)
                    }
                }
                Spacer(Modifier.height(WakepointSpacing.xs))
                Text(
                    text = if (place.isPublic) {
                        "공개 중 · 친구에겐 '${place.sharedName ?: "장소"}'로 보여요"
                    } else {
                        "비공개 · 나만 볼 수 있어요"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextSecondary,
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = WakepointIconLight,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AddSlotRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
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
        Icon(icon, contentDescription = null, tint = WakepointIconMuted, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = WakepointTextTertiary)
    }
}
