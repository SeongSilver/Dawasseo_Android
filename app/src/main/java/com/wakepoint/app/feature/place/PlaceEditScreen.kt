package com.wakepoint.app.feature.place

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.core.design.MapMarkerPreview
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMapBase
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSizeV2
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.place.PlaceRules
import com.wakepoint.app.data.location.PlaceSearchResult

/**
 * v2 STEP2 (2-2/2-3/2-4) — 장소 등록·편집.
 * 신규: 주소 검색으로 위치 선택. 편집: 주소 잠금(자물쇠).
 * 공개 ON 시 연파랑 패널 전개(친구가 보는 이름 필수 + 방패 안내 + 친구 화면 미리보기).
 * 집 슬롯은 이름 "집" 고정·삭제 불가. 삭제는 [PlaceDeleteDialog] 확인 후.
 */
@Composable
fun PlaceEditScreen(
    onBack: () -> Unit,
    viewModel: PlaceEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val original = uiState.original
    val isNew = original == null

    var myName by remember(original) { mutableStateOf(original?.myName.orEmpty()) }
    var isPublic by remember(original) { mutableStateOf(original?.isPublic ?: false) }
    var sharedName by remember(original) { mutableStateOf(original?.sharedName.orEmpty()) }
    var selectedAddress by remember(original) { mutableStateOf(original?.address.orEmpty()) }
    var selectedLat by remember(original) { mutableStateOf(original?.latitude ?: 0.0) }
    var selectedLng by remember(original) { mutableStateOf(original?.longitude ?: 0.0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.done) {
        if (uiState.done) onBack()
    }

    uiState.errorMessage?.let { message ->
        WakepointMessageDialog(message = message, onDismiss = viewModel::clearError)
    }

    val saveEnabled = !uiState.isSaving && selectedAddress.isNotBlank() &&
        PlaceRules.isSavable(myName, isPublic, sharedName, uiState.isHome)

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
                text = when {
                    uiState.isHome -> "집 ${if (isNew) "등록" else "편집"}"
                    isNew -> "장소 등록"
                    else -> "장소 편집"
                },
                style = MaterialTheme.typography.titleSmall,
                color = WakepointInk,
            )
        }

        if (uiState.isLoading) {
            WakepointLoadingPlaceholder()
            return@Column
        }

        Column(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xl),
        ) {
            // 지도 프리뷰(위치 선택 여부 표시)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WakepointSizeV2.mapPreviewHeight)
                    .clip(WakepointShape.card)
                    .background(WakepointMapBase),
                contentAlignment = Alignment.Center,
            ) {
                if (selectedAddress.isNotBlank()) {
                    MapMarkerPreview()
                } else {
                    Text(
                        "아래에서 주소를 검색해 위치를 정해주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WakepointTextSecondary,
                    )
                }
            }

            if (!uiState.isHome) {
                Field(label = "내가 보는 이름") {
                    WakepointTextField(
                        value = myName,
                        onValueChange = { myName = it },
                        placeholder = "예) 본가, 회사",
                    )
                }
            }

            if (isNew) {
                Field(label = "주소 검색") {
                    Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                        WakepointTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            placeholder = "도로명, 건물명, 지역명",
                            leadingIcon = Icons.Rounded.Search,
                            modifier = Modifier.weight(1f),
                        )
                        WakepointButton(
                            text = if (uiState.isSearching) "검색 중" else "검색",
                            enabled = !uiState.isSearching && uiState.searchQuery.isNotBlank(),
                            onClick = viewModel::searchAddress,
                            modifier = Modifier.width(86.dp).height(WakepointSize.fieldHeight),
                        )
                    }
                    uiState.searchMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = WakepointMuted)
                    }
                    uiState.searchResults.forEach { result ->
                        SearchResultRow(result = result, onClick = {
                            selectedAddress = result.address
                            selectedLat = result.lat
                            selectedLng = result.lng
                            if (myName.isBlank() && !uiState.isHome) myName = result.name
                            viewModel.clearSearchResults()
                        })
                    }
                }
            }

            Field(label = "주소") {
                WakepointTextField(
                    value = selectedAddress.ifBlank { "주소를 검색해 주세요" },
                    onValueChange = {},
                    placeholder = "주소",
                    readOnly = true,
                    leadingIcon = Icons.Rounded.Lock,
                )
                Text(
                    "주소는 나만 볼 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextSecondary,
                )
            }

            // 친구에게 공개 토글(2-3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WakepointSize.rowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("친구에게 공개", style = MaterialTheme.typography.titleSmall, color = WakepointInk)
                    Text("주소는 공개되지 않아요", style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
                }
                WakepointSwitch(checked = isPublic, onCheckedChange = {
                    isPublic = it
                    if (it && sharedName.isBlank()) sharedName = uiState.suggestedSharedName
                })
            }

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
                        WakepointTextField(
                            value = sharedName,
                            onValueChange = { sharedName = it },
                            placeholder = "예) ${uiState.suggestedSharedName}",
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap),
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            "친구는 이 이름만 볼 수 있고, 실제 주소와 위치는 알 수 없어요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WakepointTextSecondary,
                        )
                    }
                    FriendPreviewCard(sharedName = sharedName.ifBlank { uiState.suggestedSharedName })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            ) {
                if (!isNew && !uiState.isHome) {
                    WakepointDangerButton(
                        text = "삭제",
                        enabled = !uiState.isSaving,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                }
                WakepointButton(
                    text = "저장",
                    enabled = saveEnabled,
                    onClick = {
                        viewModel.save(
                            myName = myName,
                            isPublic = isPublic,
                            sharedName = sharedName,
                            address = selectedAddress,
                            latitude = selectedLat,
                            longitude = selectedLng,
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showDeleteDialog && original != null) {
        PlaceDeleteDialog(
            placeName = original.myName,
            sharedName = original.sharedName.takeIf { original.isPublic },
            onCancel = { showDeleteDialog = false },
            onDelete = {
                showDeleteDialog = false
                viewModel.delete()
            },
        )
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
private fun SearchResultRow(result: PlaceSearchResult, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(WakepointCanvas)
            .clickable(onClick = onClick)
            .padding(WakepointSpacing.md),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs),
    ) {
        Text(result.name, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
        Text(result.address, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
    }
}

/** 공개 시 친구 화면에서 어떻게 보이는지 미리보기(2-3). */
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
            Text("내 장소 (친구 화면 미리보기)", style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
        }
        Text("알람 보내기", style = MaterialTheme.typography.labelLarge, color = WakepointPrimary)
    }
}
