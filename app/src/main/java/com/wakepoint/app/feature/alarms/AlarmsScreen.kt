package com.wakepoint.app.feature.alarms

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.StatusDot
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointBorder
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointCircleIconButton
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointStar
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextDisabled
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.AlarmSound
import com.wakepoint.app.domain.model.SoundKind
import com.wakepoint.app.feature.home.ALARM_RADIUS_OPTIONS
import com.wakepoint.app.feature.timealarm.AlarmTypeSheetContent
import com.wakepoint.app.feature.timealarm.TimeAlarmCreateSheetContent

/** 리스트 하단이 떠 있는 탭 바에 가리지 않도록 확보하는 스크롤 여백(구조적 오프셋). */
private val BOTTOM_LIST_CLEARANCE = 120.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onOpenSoundList: (String?) -> Unit,
    onCreateAlarm: () -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedAlarmId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<Alarm?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchDelete by remember { mutableStateOf(false) }
    // v2 STEP1: + 버튼 → 타입 선택(1-1) → 시간 알람이면 생성 시트(1-2), 위치 알람이면 지도 직행.
    var showTypeSheet by remember { mutableStateOf(false) }
    var showTimeCreateSheet by remember { mutableStateOf(false) }

    fun exitSelection() {
        selectionMode = false
        selectedIds = emptySet()
    }

    // 동기화 등으로 알람이 사라지면 선택 집합에서 유효하지 않은 id를 정리한다.
    LaunchedEffect(uiState.alarms) {
        if (selectionMode) {
            val existing = uiState.alarms.mapTo(mutableSetOf()) { it.id }
            val pruned = selectedIds intersect existing
            if (pruned != selectedIds) selectedIds = pruned
            if (uiState.alarms.isEmpty()) exitSelection()
        }
    }

    BackHandler(enabled = selectionMode) { exitSelection() }

    val summary = when {
        uiState.alarms.isEmpty() -> null
        uiState.activeAlarms.isNotEmpty() ->
            stringResource(R.string.alarms_summary_active, uiState.activeAlarms.size)
        else -> stringResource(R.string.alarms_summary_none)
    }

    uiState.errorMessage?.let { message ->
        WakepointMessageDialog(message = message, onDismiss = viewModel::clearError)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground)
    ) {
        if (selectionMode) {
            AlarmSelectionHeader(
                count = selectedIds.size,
                allSelected = uiState.alarms.isNotEmpty() && selectedIds.size == uiState.alarms.size,
                onClose = { exitSelection() },
                onToggleSelectAll = {
                    selectedIds = if (selectedIds.size == uiState.alarms.size) {
                        emptySet()
                    } else {
                        uiState.alarms.mapTo(mutableSetOf()) { it.id }
                    }
                },
                onDelete = { showBatchDelete = true }
            )
        } else {
            WakepointPageHeader(
                title = stringResource(R.string.alarms_title),
                subtitle = summary,
                action = {
                    WakepointCircleIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.alarm_add),
                        onClick = { showTypeSheet = true }
                    )
                }
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(top = WakepointSpacing.sectionTitleGap, bottom = BOTTOM_LIST_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
        ) {
            if (uiState.isLoading) {
                WakepointLoadingPlaceholder()
            } else if (uiState.alarms.isEmpty()) {
                EmptyAlarmMessage(onCreateAlarm = { showTypeSheet = true })
            } else {
                val renderAlarm: @Composable (com.wakepoint.app.domain.model.Alarm) -> Unit = { alarm ->
                    key(alarm.id) {
                        AlarmListCard(
                            alarm = alarm,
                            soundTitle = alarm.resolveSoundTitle(uiState.soundTitles),
                            selectionMode = selectionMode,
                            selected = alarm.id in selectedIds,
                            expanded = !selectionMode && expandedAlarmId == alarm.id,
                            isUpdating = alarm.id in uiState.updatingAlarmIds,
                            onToggleSelect = {
                                selectedIds = if (alarm.id in selectedIds) {
                                    selectedIds - alarm.id
                                } else {
                                    selectedIds + alarm.id
                                }
                            },
                            onEnterSelection = {
                                expandedAlarmId = null
                                selectionMode = true
                                selectedIds = setOf(alarm.id)
                            },
                            onToggleExpanded = {
                                expandedAlarmId = if (expandedAlarmId == alarm.id) null else alarm.id
                            },
                            onOpenSoundList = { onOpenSoundList(alarm.id) },
                            onActiveChange = { isActive -> viewModel.setAlarmActive(alarm.id, isActive) },
                            onToggleFavorite = { viewModel.setFavorite(alarm.id, !alarm.isFavorite) },
                            onSave = { updatedAlarm, label, radius, vibrate, repeating ->
                                viewModel.updateAlarmSettings(updatedAlarm, label, radius, vibrate, repeating)
                            },
                            onDeleteRequest = { deleteTarget = alarm }
                        )
                    }
                }

                if (uiState.favoriteAlarms.isNotEmpty()) {
                    SectionTitle(text = stringResource(R.string.alarm_favorite))
                    uiState.favoriteAlarms.forEach { renderAlarm(it) }
                }

                if (uiState.activeAlarms.isNotEmpty()) {
                    SectionTitle(text = stringResource(R.string.alarm_active))
                    uiState.activeAlarms.forEach { renderAlarm(it) }
                }

                if (uiState.inactiveAlarms.isNotEmpty()) {
                    SectionTitle(text = stringResource(R.string.alarm_inactive))
                    uiState.inactiveAlarms.forEach { renderAlarm(it) }
                }
            }
        }
    }

    deleteTarget?.let { alarm ->
        WakepointConfirmDialog(
            title = stringResource(R.string.alarm_delete_confirm_title),
            message = stringResource(R.string.alarm_delete_confirm_message),
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = stringResource(R.string.alarm_delete),
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteAlarm(alarm.id)
                if (expandedAlarmId == alarm.id) {
                    expandedAlarmId = null
                }
                deleteTarget = null
            }
        )
    }

    if (showBatchDelete) {
        WakepointConfirmDialog(
            title = stringResource(R.string.alarm_delete_confirm_title),
            message = stringResource(R.string.alarm_delete_confirm_message_multi, selectedIds.size),
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = stringResource(R.string.alarm_delete),
            onDismiss = { showBatchDelete = false },
            onConfirm = {
                viewModel.deleteAlarms(selectedIds)
                showBatchDelete = false
                exitSelection()
            }
        )
    }

    if (showTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas
        ) {
            AlarmTypeSheetContent(
                onSelectLocation = {
                    showTypeSheet = false
                    onCreateAlarm()
                },
                onSelectTime = {
                    showTypeSheet = false
                    showTimeCreateSheet = true
                },
                onCancel = { showTypeSheet = false }
            )
        }
    }

    if (showTimeCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimeCreateSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas
        ) {
            TimeAlarmCreateSheetContent(
                onCancel = { showTimeCreateSheet = false },
                onAdd = { alarm ->
                    viewModel.saveTimeAlarm(alarm)
                    showTimeCreateSheet = false
                }
            )
        }
    }
}

@Composable
private fun AlarmSelectionHeader(
    count: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WakepointSpacing.md)
            .padding(top = WakepointSpacing.md, bottom = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.alarm_selection_close),
                tint = WakepointInk
            )
        }
        Text(
            text = stringResource(R.string.alarm_selection_count, count),
            style = MaterialTheme.typography.titleMedium,
            color = WakepointInk,
            modifier = Modifier
                .weight(1f)
                .padding(start = WakepointSpacing.xs)
        )
        IconButton(onClick = onToggleSelectAll) {
            Icon(
                imageVector = Icons.Rounded.SelectAll,
                contentDescription = stringResource(R.string.alarm_select_all),
                tint = if (allSelected) WakepointPrimary else WakepointInk
            )
        }
        IconButton(onClick = onDelete, enabled = count > 0) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.alarm_delete_selected),
                tint = if (count > 0) WakepointDanger else WakepointMuted
            )
        }
    }
}

@Composable
private fun AlarmListCard(
    alarm: Alarm,
    soundTitle: String,
    selectionMode: Boolean,
    selected: Boolean,
    expanded: Boolean,
    isUpdating: Boolean,
    onToggleSelect: () -> Unit,
    onEnterSelection: () -> Unit,
    onToggleExpanded: () -> Unit,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit,
    onSave: (Alarm, String, String, Boolean, Boolean) -> Unit,
    onDeleteRequest: () -> Unit
) {
    when {
        selectionMode -> SelectableAlarmCard(
            alarm = alarm,
            soundTitle = soundTitle,
            selected = selected,
            onToggleSelect = onToggleSelect
        )
        expanded -> EditableAlarmCard(
            alarm = alarm,
            soundTitle = soundTitle,
            isUpdating = isUpdating,
            onToggleExpanded = onToggleExpanded,
            onOpenSoundList = onOpenSoundList,
            onActiveChange = onActiveChange,
            onToggleFavorite = onToggleFavorite,
            onSave = onSave,
            onDelete = onDeleteRequest
        )
        else -> CompactAlarmCard(
            alarm = alarm,
            soundTitle = soundTitle,
            isUpdating = isUpdating,
            onToggleExpanded = onToggleExpanded,
            onLongClick = onEnterSelection,
            onActiveChange = onActiveChange,
            onToggleFavorite = onToggleFavorite
        )
    }
}

/** 선택 모드에서 표시되는 카드. 카드 전체를 탭하면 선택이 토글되고, 왼쪽 체크로 상태를 표시한다. */
@Composable
private fun SelectableAlarmCard(
    alarm: Alarm,
    soundTitle: String,
    selected: Boolean,
    onToggleSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .clickable(onClick = onToggleSelect),
        shape = WakepointShape.card,
        color = if (selected) WakepointPrimaryLight else WakepointCanvas,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) WakepointPrimary else WakepointBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(WakepointSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.alarm_select_item),
                tint = if (selected) WakepointPrimary else WakepointMuted,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(WakepointSpacing.md))
            AlarmCardInfo(
                alarm = alarm,
                soundTitle = soundTitle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 컴팩트/선택 카드가 공유하는 알람 요약 정보(상태 점·별칭·주소·메타). */
@Composable
private fun AlarmCardInfo(
    alarm: Alarm,
    soundTitle: String,
    modifier: Modifier = Modifier
) {
    val radiusLabel = if (alarm.radiusKm < 1.0) {
        "${(alarm.radiusKm * 1000).toInt()}m"
    } else {
        "${alarm.radiusKm.toInt()}km"
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
        ) {
            StatusDot(active = alarm.isActive, size = 7)
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (alarm.isActive) WakepointPrimary else WakepointTextDisabled
            )
        }
        Text(
            text = alarm.targetAddress,
            style = MaterialTheme.typography.titleMedium,
            color = if (alarm.isActive) WakepointInk else WakepointTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.alarm_meta_line, radiusLabel, soundTitle),
            color = WakepointTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FavoriteStarButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.alarm_favorite_remove else R.string.alarm_favorite_add
            ),
            tint = if (isFavorite) WakepointStar else WakepointMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun EditableAlarmCard(
    alarm: Alarm,
    soundTitle: String,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit,
    onSave: (Alarm, String, String, Boolean, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var radius by remember(alarm.radiusKm) { mutableStateOf(alarm.radiusKm.toRadiusOption()) }
    var label by remember(alarm.id, alarm.label) { mutableStateOf(alarm.label) }
    var vibrate by remember(alarm.id, alarm.vibrate) { mutableStateOf(alarm.vibrate) }
    var repeating by remember(alarm.id, alarm.repeating) { mutableStateOf(alarm.repeating) }
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap)) {
            // 헤더: "위치 별칭 :" 라벨 노출 제거 — 값만 T3(카드 제목)로 표기
            Row(
                modifier = Modifier.clickable(onClick = onToggleExpanded),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.label,
                    color = WakepointInk,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                FavoriteStarButton(isFavorite = alarm.isFavorite, onClick = onToggleFavorite)
                WakepointSwitch(
                    checked = alarm.isActive,
                    onCheckedChange = onActiveChange,
                    enabled = !isUpdating
                )
            }
            EditSection(label = stringResource(R.string.alarm_location_alias)) {
                WakepointTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = stringResource(R.string.alarm_location_alias),
                    readOnly = isUpdating
                )
            }
            EditSection(label = stringResource(R.string.alarm_radius_setting)) {
                RadiusSelector(
                    options = ALARM_RADIUS_OPTIONS,
                    selectedOption = radius,
                    onSelected = { radius = it }
                )
            }
            EditSection(label = stringResource(R.string.alarm_sound_setting)) {
                SoundOptionRow(
                    title = soundTitle,
                    icon = Icons.Rounded.Notifications,
                    trailing = {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = WakepointMuted
                        )
                    },
                    onClick = onOpenSoundList
                )
            }
            EditToggleRow(
                title = stringResource(R.string.alarm_vibrate_setting),
                hint = stringResource(R.string.alarm_vibrate_hint),
                checked = vibrate,
                onCheckedChange = { vibrate = it },
                enabled = !isUpdating
            )
            EditToggleRow(
                title = stringResource(R.string.alarm_repeating_setting),
                hint = stringResource(R.string.alarm_repeating_hint),
                checked = repeating,
                onCheckedChange = { repeating = it },
                enabled = !isUpdating
            )
            // 2분할 1:1 · 삭제(Danger Tonal) 왼쪽 · 긍정 액션(저장) 오른쪽
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                WakepointDangerButton(
                    text = stringResource(R.string.alarm_delete),
                    enabled = !isUpdating,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
                WakepointButton(
                    text = stringResource(R.string.alarm_save),
                    enabled = !isUpdating,
                    onClick = { onSave(alarm, label, radius, vibrate, repeating) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 편집 카드 섹션: 섹션 타이틀(T2) + 콘텐츠, 라벨↔콘텐츠 8. */
@Composable
private fun EditSection(
    label: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        SectionLabel(text = label)
        content()
    }
}

/** 편집 카드 토글 행: 제목(15/SemiBold) + 보조설명(T5) + 스위치, 높이 56. */
@Composable
private fun EditToggleRow(
    title: String,
    hint: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = WakepointInk,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = hint,
                color = WakepointTextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        WakepointSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactAlarmCard(
    alarm: Alarm,
    soundTitle: String,
    isUpdating: Boolean,
    onToggleExpanded: () -> Unit,
    onLongClick: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit
) {
    WakepointCard(
        modifier = Modifier.combinedClickable(
            onClick = onToggleExpanded,
            onLongClick = onLongClick
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlarmCardInfo(
                alarm = alarm,
                soundTitle = soundTitle,
                modifier = Modifier.weight(1f)
            )
            FavoriteStarButton(isFavorite = alarm.isFavorite, onClick = onToggleFavorite)
            WakepointSwitch(
                checked = alarm.isActive,
                onCheckedChange = onActiveChange,
                enabled = !isUpdating
            )
        }
    }
}

@Composable
private fun EmptyAlarmMessage(onCreateAlarm: () -> Unit) {
    WakepointCard(contentPadding = 24) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = WakepointShape.card,
                color = WakepointParchment
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = WakepointTextTertiary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.alarm_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            Text(
                text = stringResource(R.string.alarm_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            WakepointButton(
                text = stringResource(R.string.alarm_empty_action),
                icon = Icons.Rounded.Add,
                onClick = onCreateAlarm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = WakepointInk,
        modifier = Modifier.padding(top = WakepointSpacing.sm)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundListScreen(
    onBack: () -> Unit,
    viewModel: SoundListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var deleteTarget by remember { mutableStateOf<AlarmSound?>(null) }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPreview() }
    }

    val permissionDeniedMessage = stringResource(R.string.sound_permission_denied)
    val audioReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importSound(uri)
    }
    val importPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            importLauncher.launch(AUDIO_MIME_TYPES)
        } else {
            viewModel.reportError(permissionDeniedMessage)
        }
    }
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    fun requestStartRecording() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startRecording()
        } else {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun requestImportSound() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            audioReadPermission
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            importLauncher.launch(AUDIO_MIME_TYPES)
        } else {
            importPermissionLauncher.launch(audioReadPermission)
        }
    }

    val systemSoundPickerTitle = stringResource(R.string.sound_system_picker_title)
    val systemSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked = result.data?.let { data ->
                IntentCompat.getParcelableExtra(
                    data,
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            }
            if (picked != null) viewModel.addSystemSound(picked)
        }
    }

    fun openSystemSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, systemSoundPickerTitle)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        runCatching { systemSoundLauncher.launch(intent) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(WakepointCanvas)
                .padding(horizontal = WakepointSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.stopPreview()
                onBack()
            }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.alarm_sound_setting),
                style = MaterialTheme.typography.titleSmall,
                color = WakepointInk
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = WakepointMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionTitle(text = stringResource(R.string.sound_section_bundled))
            if (uiState.bundledSounds.isEmpty()) {
                Text(
                    text = stringResource(R.string.sound_bundled_empty),
                    color = WakepointTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = WakepointSpacing.xs)
                )
            } else {
                uiState.bundledSounds.forEach { sound ->
                    key(sound.id) {
                        SelectableSoundRow(
                            sound = sound,
                            selected = sound.uri == uiState.selectedUri,
                            onSelect = { viewModel.selectSound(sound) },
                            onDelete = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WakepointSpacing.sm))
            SectionTitle(text = stringResource(R.string.sound_section_custom))
            if (uiState.customSounds.isEmpty()) {
                Text(
                    text = stringResource(R.string.sound_custom_empty),
                    color = WakepointTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = WakepointSpacing.xs)
                )
            } else {
                uiState.customSounds.forEach { sound ->
                    key(sound.id) {
                        SelectableSoundRow(
                            sound = sound,
                            selected = sound.uri == uiState.selectedUri,
                            onSelect = { viewModel.selectSound(sound) },
                            onDelete = { deleteTarget = sound }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WakepointCanvas)
                .navigationBarsPadding()
                .padding(WakepointSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            WakepointSecondaryButton(
                text = stringResource(R.string.sound_system),
                icon = Icons.Rounded.Alarm,
                onClick = { openSystemSoundPicker() },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                WakepointSecondaryButton(
                    text = stringResource(R.string.sound_import),
                    icon = Icons.Rounded.FileDownload,
                    onClick = { requestImportSound() },
                    modifier = Modifier.weight(1f)
                )
                WakepointButton(
                    text = stringResource(R.string.sound_record),
                    icon = Icons.Rounded.Mic,
                    onClick = { viewModel.openRecordSheet() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (uiState.recordSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRecordSheet() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas
        ) {
            RecordingSheet(
                phase = uiState.recordingPhase,
                elapsedSeconds = uiState.elapsedSeconds,
                onStart = { requestStartRecording() },
                onStop = { viewModel.stopRecording() },
                onRetry = { viewModel.retryRecording() },
                onSave = { viewModel.saveRecording() },
                onCancel = { viewModel.closeRecordSheet() }
            )
        }
    }

    deleteTarget?.let { sound ->
        WakepointConfirmDialog(
            title = stringResource(R.string.sound_delete_confirm_title),
            message = stringResource(R.string.sound_delete_confirm_message),
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = stringResource(R.string.sound_delete),
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteSound(sound)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun SelectableSoundRow(
    sound: AlarmSound,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    SoundOptionRow(
        title = sound.title,
        subtitle = sound.subtitle,
        icon = sound.iconVector(),
        selected = selected,
        onClick = onSelect,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = WakepointPrimary
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.sound_delete),
                            tint = WakepointMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    )
}

private fun AlarmSound.iconVector(): ImageVector = when (kind) {
    SoundKind.Bundled -> Icons.Rounded.Notifications
    SoundKind.Recorded -> Icons.Rounded.GraphicEq
    SoundKind.Imported -> Icons.Rounded.AudioFile
    SoundKind.System -> Icons.Rounded.Alarm
}

@Composable
private fun RecordingSheet(
    phase: RecordingPhase,
    elapsedSeconds: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WakepointSpacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap)
    ) {
        Text(
            text = stringResource(R.string.record_title),
            color = WakepointPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = formatElapsed(elapsedSeconds),
            color = WakepointPrimary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = when (phase) {
                RecordingPhase.Idle -> stringResource(R.string.record_hint)
                RecordingPhase.Recording -> stringResource(R.string.record_recording)
                RecordingPhase.Recorded -> stringResource(R.string.record_done)
            },
            color = WakepointTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        when (phase) {
            RecordingPhase.Idle -> {
                RecordCircleButton(
                    icon = Icons.Rounded.Mic,
                    contentDescription = stringResource(R.string.record_start),
                    onClick = onStart
                )
            }
            RecordingPhase.Recording -> {
                RecordCircleButton(
                    icon = Icons.Rounded.Stop,
                    contentDescription = stringResource(R.string.record_stop),
                    onClick = onStop
                )
            }
            RecordingPhase.Recorded -> {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = WakepointPrimary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        HorizontalDivider(color = WakepointParchment)
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointSecondaryButton(
                text = stringResource(
                    if (phase == RecordingPhase.Recorded) R.string.record_retry else R.string.alarm_cancel
                ),
                onClick = if (phase == RecordingPhase.Recorded) onRetry else onCancel,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.alarm_save),
                enabled = phase == RecordingPhase.Recorded,
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecordCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(2.dp, WakepointPrimary),
        color = WakepointCanvas
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = WakepointPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private fun formatElapsed(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
}

private fun Double.toRadiusOption(): String {
    return if (this < 1.0) {
        "${(this * 1000).toInt()}m"
    } else {
        "${this.toInt()}km"
    }
}

/** 알람에 저장된 사운드 URI를 사람이 읽을 수 있는 이름으로 변환. 없으면 기본 벨소리. */
private fun Alarm.resolveSoundTitle(titles: Map<String, String>): String =
    soundUri?.let { titles[it] } ?: "기본 벨소리"

/** 오디오 파일 선택기에서 허용할 MIME 타입. */
private val AUDIO_MIME_TYPES = arrayOf("audio/*")
