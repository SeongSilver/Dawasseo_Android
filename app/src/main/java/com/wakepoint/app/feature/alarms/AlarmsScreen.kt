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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.RadiusSelector
import com.wakepoint.app.core.design.SoundOptionRow
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointCircleIconButton
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointEmptyState
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMessage
import com.wakepoint.app.core.design.WakepointMessageKind
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointScreenPreviews
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointSwitch
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTintButton
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.AlarmSound
import com.wakepoint.app.domain.model.SoundKind
import com.wakepoint.app.domain.model.SoundType
import com.wakepoint.app.domain.model.TimeAlarm
import com.wakepoint.app.feature.friends.ReceiveAlarmDialog
import com.wakepoint.app.feature.friends.ReceivedAlarmLogic
import com.wakepoint.app.feature.friends.acknowledgeReceivedAlarms
import com.wakepoint.app.feature.friends.acknowledgedReceivedAlarmIds
import com.wakepoint.app.feature.friends.isReceivedAlarmSeeded
import com.wakepoint.app.feature.friends.markReceivedAlarmSeeded
import com.wakepoint.app.feature.home.ALARM_RADIUS_OPTIONS
import com.wakepoint.app.feature.timealarm.AlarmTypeSheetContent
import com.wakepoint.app.feature.timealarm.ExactAlarmPermissionDialog
import com.wakepoint.app.feature.timealarm.TimeAlarmCard
import com.wakepoint.app.feature.timealarm.TimeAlarmCreateSheetContent
import com.wakepoint.app.feature.timealarm.isExactAlarmPrompted
import com.wakepoint.app.feature.timealarm.markExactAlarmPrompted
import com.wakepoint.app.feature.timealarm.openExactAlarmSettings
import com.wakepoint.app.feature.timealarm.shouldPromptExactAlarm

/** 하단 탭바(80dp + 내비게이션바 인셋) 위로 목록이 가리지 않도록 확보하는 구조 상수. */
private val BOTTOM_TAB_CLEARANCE = 80.dp

/** 시트·화면 하단 버튼 줄 비율 — 보조 1 : 실행 1.4 (design-handoff `Alarms-edit`·`Sounds-recording`). */
private const val BOTTOM_SECONDARY_WEIGHT = 1f
private const val BOTTOM_PRIMARY_WEIGHT = 1.4f

/** 녹음 시트 가운데 정지/시작 버튼 폭 (design-handoff `Sounds-recording`, 구조 상수). */
private val RECORD_BUTTON_WIDTH = 160.dp

// ─────────────────────────────────────────────────────────────────────
// 알람 목록 (design-handoff `Alarms-list` · `-open` · `-edit` · `-empty` · `-updating` · `-delete`)
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onOpenSoundList: (String?) -> Unit,
    onCreateAlarm: () -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedAlarmId by remember { mutableStateOf<String?>(null) }
    // DESIGN.md 8절 기본값 = B안(별도 수정 화면). 카드를 누르면 `Alarms-edit` 화면이 열린다.
    var editingAlarmId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<Alarm?>(null) }
    var deleteTimeTarget by remember { mutableStateOf<TimeAlarm?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchDelete by remember { mutableStateOf(false) }
    // v2 STEP1: + 버튼 → 타입 선택(1-1) → 시간 알람이면 생성 시트(1-2), 위치 알람이면 지도 직행.
    var showTypeSheet by remember { mutableStateOf(false) }
    var showTimeCreateSheet by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 설정 화면에서 정확 알람을 허용하고 돌아오면 배지가 바로 사라지도록 onResume 마다 재확인.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshExactAlarmState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

    val editingAlarm = uiState.alarms.firstOrNull { it.id == editingAlarmId }
    BackHandler(enabled = editingAlarm != null) { editingAlarmId = null }

    if (editingAlarm != null) {
        AlarmEditContent(
            alarm = editingAlarm,
            soundTitle = editingAlarm.resolveSoundTitle(uiState.soundTitles),
            isUpdating = editingAlarm.id in uiState.updatingAlarmIds,
            errorMessage = uiState.errorMessage,
            onBack = { editingAlarmId = null },
            onOpenSoundList = { onOpenSoundList(editingAlarm.id) },
            onActiveChange = { viewModel.setAlarmActive(editingAlarm.id, it) },
            onVibrateChange = { viewModel.updateAlarmSettings(editingAlarm, editingAlarm.label, editingAlarm.radiusKm.toRadiusOption(), it, editingAlarm.repeating) },
            onRepeatingChange = { viewModel.updateAlarmSettings(editingAlarm, editingAlarm.label, editingAlarm.radiusKm.toRadiusOption(), editingAlarm.vibrate, it) },
            onDelete = { deleteTarget = editingAlarm },
            onSave = { label, radius ->
                viewModel.updateAlarmSettings(editingAlarm, label, radius, editingAlarm.vibrate, editingAlarm.repeating)
                editingAlarmId = null
            }
        )
    } else {
        AlarmsListContent(
            uiState = uiState,
            selectionMode = selectionMode,
            selectedIds = selectedIds,
            expandedAlarmId = expandedAlarmId,
            onAdd = { showTypeSheet = true },
            onExitSelection = { exitSelection() },
            onToggleSelectAll = {
                selectedIds = if (selectedIds.size == uiState.alarms.size) {
                    emptySet()
                } else {
                    uiState.alarms.mapTo(mutableSetOf()) { it.id }
                }
            },
            onDeleteSelected = { showBatchDelete = true },
            onToggleSelect = { alarm ->
                selectedIds = if (alarm.id in selectedIds) selectedIds - alarm.id else selectedIds + alarm.id
            },
            onEnterSelection = { alarm ->
                expandedAlarmId = null
                selectionMode = true
                selectedIds = setOf(alarm.id)
            },
            onOpenAlarm = { alarm -> editingAlarmId = alarm.id },
            onCollapse = { expandedAlarmId = null },
            onOpenSoundList = { alarm -> onOpenSoundList(alarm.id) },
            onActiveChange = { alarm, active -> viewModel.setAlarmActive(alarm.id, active) },
            onToggleFavorite = { alarm -> viewModel.setFavorite(alarm.id, !alarm.isFavorite) },
            onSaveExpanded = { alarm, label, radius ->
                viewModel.updateAlarmSettings(alarm, label, radius, alarm.vibrate, alarm.repeating)
                expandedAlarmId = null
            },
            onDeleteRequest = { alarm -> deleteTarget = alarm },
            timeAlarmSlot = { alarm ->
                TimeAlarmCard(
                    alarm = alarm,
                    expanded = !selectionMode && expandedAlarmId == alarm.id,
                    isUpdating = alarm.id in uiState.updatingAlarmIds,
                    exactAlarmAllowed = uiState.exactAlarmAllowed,
                    onToggleExpanded = {
                        expandedAlarmId = if (expandedAlarmId == alarm.id) null else alarm.id
                    },
                    onEnabledChange = { enabled -> viewModel.setTimeAlarmEnabled(alarm.id, enabled) },
                    onSave = { viewModel.saveTimeAlarm(it) },
                    onDeleteRequest = { deleteTimeTarget = alarm },
                    onAccuracyBadgeClick = { context.openExactAlarmSettings() }
                )
            }
        )
    }

    deleteTarget?.let { alarm ->
        AlarmDeleteDialog(
            message = stringResource(R.string.alarm_delete_confirm_message),
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteAlarm(alarm.id)
                if (expandedAlarmId == alarm.id) expandedAlarmId = null
                if (editingAlarmId == alarm.id) editingAlarmId = null
                deleteTarget = null
            }
        )
    }

    deleteTimeTarget?.let { alarm ->
        AlarmDeleteDialog(
            message = stringResource(R.string.alarm_delete_confirm_message),
            onDismiss = { deleteTimeTarget = null },
            onConfirm = {
                viewModel.deleteTimeAlarm(alarm.id)
                if (expandedAlarmId == alarm.id) expandedAlarmId = null
                deleteTimeTarget = null
            }
        )
    }

    if (showBatchDelete) {
        AlarmDeleteDialog(
            message = stringResource(R.string.alarm_delete_confirm_message_multi, selectedIds.size),
            onDismiss = { showBatchDelete = false },
            onConfirm = {
                viewModel.deleteAlarms(selectedIds)
                showBatchDelete = false
                exitSelection()
            }
        )
    }

    // v2 STEP3 (3-7): 친구가 보낸 알람 수신 다이얼로그 — 동기화로 내려온 미확인 친구 알람을
    // 풀 기반으로 감지한다(푸시 유실과 무관). 최초 실행 시엔 기존 수신분을 시드해 폭주를 막는다.
    var receivedAckVersion by remember { mutableStateOf(0) }
    LaunchedEffect(uiState.currentUserId, uiState.alarms) {
        if (uiState.currentUserId.isNotBlank() && !context.isReceivedAlarmSeeded()) {
            context.acknowledgeReceivedAlarms(
                ReceivedAlarmLogic.idsToSeed(uiState.alarms, uiState.currentUserId)
            )
            context.markReceivedAlarmSeeded()
            receivedAckVersion++
        }
    }
    val receivedAlarm = remember(uiState.alarms, uiState.currentUserId, receivedAckVersion) {
        if (!context.isReceivedAlarmSeeded()) {
            null
        } else {
            ReceivedAlarmLogic.newReceived(
                alarms = uiState.alarms,
                currentUserId = uiState.currentUserId,
                acknowledgedIds = context.acknowledgedReceivedAlarmIds()
            ).firstOrNull()
        }
    }
    receivedAlarm?.let { alarm ->
        ReceiveAlarmDialog(
            alarm = alarm,
            senderName = uiState.friendNames[alarm.createdBy]
                ?: stringResource(R.string.friends_title),
            onActivate = {
                context.acknowledgeReceivedAlarms(setOf(alarm.id))
                if (!alarm.isActive) viewModel.setAlarmActive(alarm.id, true)
                receivedAckVersion++
            },
            onReject = {
                context.acknowledgeReceivedAlarms(setOf(alarm.id))
                viewModel.deleteAlarm(alarm.id)
                receivedAckVersion++
            }
        )
    }

    if (showExactAlarmDialog) {
        ExactAlarmPermissionDialog(
            onGoToSettings = {
                showExactAlarmDialog = false
                context.markExactAlarmPrompted()
                context.openExactAlarmSettings()
            },
            onLater = {
                showExactAlarmDialog = false
                context.markExactAlarmPrompted()
            }
        )
    }

    if (showTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() },
            containerColor = LocalWakepointColors.current.surfaceElevated,
            scrimColor = LocalWakepointColors.current.dim,
            shape = WakepointShape.sheet
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
            dragHandle = { BottomSheetHandle() },
            containerColor = LocalWakepointColors.current.surfaceElevated,
            scrimColor = LocalWakepointColors.current.dim,
            shape = WakepointShape.sheet
        ) {
            TimeAlarmCreateSheetContent(
                onCancel = { showTimeCreateSheet = false },
                onAdd = { alarm ->
                    viewModel.saveTimeAlarm(alarm)
                    showTimeCreateSheet = false
                    // 1-5: 첫 시간 알람 생성 시 정확 알람 미허용이면 설치당 1회 안내.
                    if (shouldPromptExactAlarm(
                            exactAlarmAllowed = uiState.exactAlarmAllowed,
                            alreadyPrompted = context.isExactAlarmPrompted()
                        )
                    ) {
                        showExactAlarmDialog = true
                    }
                }
            )
        }
    }
}

/**
 * 알람 목록 본문 (design-handoff `Alarms-list` · `-empty` · `-updating`).
 * 상단 바는 고정, 목록만 스크롤한다 (DESIGN.md 7절).
 */
@Composable
private fun AlarmsListContent(
    uiState: AlarmsUiState,
    selectionMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    expandedAlarmId: String? = null,
    onAdd: () -> Unit = {},
    onExitSelection: () -> Unit = {},
    onToggleSelectAll: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onToggleSelect: (Alarm) -> Unit = {},
    onEnterSelection: (Alarm) -> Unit = {},
    onOpenAlarm: (Alarm) -> Unit = {},
    onCollapse: () -> Unit = {},
    onOpenSoundList: (Alarm) -> Unit = {},
    onActiveChange: (Alarm, Boolean) -> Unit = { _, _ -> },
    onToggleFavorite: (Alarm) -> Unit = {},
    onSaveExpanded: (Alarm, String, String) -> Unit = { _, _, _ -> },
    onDeleteRequest: (Alarm) -> Unit = {},
    timeAlarmSlot: @Composable (TimeAlarm) -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (selectionMode) {
            AlarmSelectionHeader(
                count = selectedIds.size,
                allSelected = uiState.alarms.isNotEmpty() && selectedIds.size == uiState.alarms.size,
                onClose = onExitSelection,
                onToggleSelectAll = onToggleSelectAll,
                onDelete = onDeleteSelected
            )
        } else {
            WakepointHeader(
                action = {
                    WakepointCircleIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.alarm_add),
                        filled = true,
                        tint = colors.text,
                        onClick = onAdd
                    )
                }
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(
                    top = WakepointSpacing.sectionTitleGap,
                    bottom = BOTTOM_TAB_CLEARANCE + WakepointSpacing.lg
                ),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
        ) {
            // 갱신 실패는 다이얼로그 대신 목록 맨 위 오류 메시지로 알린다 (`Alarms-updating`).
            uiState.errorMessage?.let { message ->
                WakepointMessage(text = message, kind = WakepointMessageKind.Error)
            }

            if (uiState.isLoading) {
                WakepointLoadingPlaceholder()
            } else if (uiState.isEmpty) {
                WakepointEmptyState(
                    icon = Icons.Rounded.Alarm,
                    title = stringResource(R.string.alarm_empty_title),
                    description = stringResource(R.string.alarm_empty_hint),
                    modifier = Modifier.padding(top = WakepointSpacing.sm)
                )
            } else {
                val renderAlarm: @Composable (Alarm) -> Unit = { alarm ->
                    key(alarm.id) {
                        when {
                            selectionMode -> SelectableAlarmCard(
                                alarm = alarm,
                                selected = alarm.id in selectedIds,
                                onToggleSelect = { onToggleSelect(alarm) }
                            )
                            expandedAlarmId == alarm.id -> ExpandedAlarmCard(
                                alarm = alarm,
                                soundTitle = alarm.resolveSoundTitle(uiState.soundTitles),
                                isUpdating = alarm.id in uiState.updatingAlarmIds,
                                onCollapse = onCollapse,
                                onOpenSoundList = { onOpenSoundList(alarm) },
                                onActiveChange = { onActiveChange(alarm, it) },
                                onSave = { label, radius -> onSaveExpanded(alarm, label, radius) },
                                onDelete = { onDeleteRequest(alarm) }
                            )
                            else -> AlarmSummaryCard(
                                alarm = alarm,
                                isUpdating = alarm.id in uiState.updatingAlarmIds,
                                onClick = { onOpenAlarm(alarm) },
                                onLongClick = { onEnterSelection(alarm) },
                                onActiveChange = { onActiveChange(alarm, it) },
                                onToggleFavorite = { onToggleFavorite(alarm) }
                            )
                        }
                    }
                }

                if (uiState.favoriteAlarms.isNotEmpty()) {
                    AlarmSectionHeader(
                        title = stringResource(R.string.alarm_favorite),
                        count = uiState.favoriteAlarms.size
                    )
                    uiState.favoriteAlarms.forEach { renderAlarm(it) }
                }

                if (uiState.activeAlarms.isNotEmpty() || uiState.activeTimeAlarms.isNotEmpty()) {
                    AlarmSectionHeader(
                        title = stringResource(R.string.alarm_active),
                        count = uiState.activeAlarms.size + uiState.activeTimeAlarms.size
                    )
                    uiState.activeAlarms.forEach { renderAlarm(it) }
                    uiState.activeTimeAlarms.forEach { key(it.id) { timeAlarmSlot(it) } }
                }

                if (uiState.inactiveAlarms.isNotEmpty() || uiState.inactiveTimeAlarms.isNotEmpty()) {
                    AlarmSectionHeader(
                        title = stringResource(R.string.alarm_inactive),
                        count = uiState.inactiveAlarms.size + uiState.inactiveTimeAlarms.size
                    )
                    uiState.inactiveAlarms.forEach { renderAlarm(it) }
                    uiState.inactiveTimeAlarms.forEach { key(it.id) { timeAlarmSlot(it) } }
                }
            }
        }
    }
}

/** 섹션 제목 — titleMedium + 개수 labelLarge, 좌 인셋 4 (design-handoff `Alarms-list`). */
@Composable
private fun AlarmSectionHeader(title: String, count: Int) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier.padding(start = WakepointSpacing.xs, top = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = colors.text)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary
        )
    }
}

/**
 * 알람 요약 카드 — 활성은 채움(`card`), 비활성은 테두리(`background` + 1dp `line`).
 * "위치: 별칭" labelLarge · 주소 titleMedium · "반경 N" bodyMedium.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmSummaryCard(
    alarm: Alarm,
    isUpdating: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit
) {
    WakepointCard(
        filled = alarm.isActive,
        modifier = Modifier
            .clip(WakepointShape.card)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlarmCardSummaryText(alarm = alarm, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(WakepointSpacing.lg))
            FavoriteStarButton(isFavorite = alarm.isFavorite, onClick = onToggleFavorite)
            Spacer(modifier = Modifier.width(WakepointSpacing.sm))
            WakepointSwitch(
                checked = alarm.isActive,
                onCheckedChange = onActiveChange,
                enabled = !isUpdating
            )
        }
    }
}

/** 요약 카드의 글 묶음 — 선택 카드와 공유한다. */
@Composable
private fun AlarmCardSummaryText(alarm: Alarm, modifier: Modifier = Modifier) {
    val colors = LocalWakepointColors.current
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.alarm_card_alias, alarm.label),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = alarm.targetAddress,
            style = MaterialTheme.typography.titleMedium,
            color = if (alarm.isActive) colors.text else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.alarm_card_radius, alarm.radiusKm.toRadiusOption()),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

/** 즐겨찾기 별 — 디자인에는 없지만 기존 기능이라 요약 카드에 남겨 둔다. */
@Composable
private fun FavoriteStarButton(isFavorite: Boolean, onClick: () -> Unit) {
    val colors = LocalWakepointColors.current
    IconButton(onClick = onClick, modifier = Modifier.size(WakepointSize.minTouchTarget)) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.alarm_favorite_remove else R.string.alarm_favorite_add
            ),
            tint = if (isFavorite) colors.brand else colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
    }
}

/**
 * A안 카드 펼침 (design-handoff `Alarms-open`).
 * 기본 경로는 B안(`AlarmEditContent`)이지만, 코드가 이미 펼치는 동작을 갖고 있어 모양만 디자인에 맞춰 둔다.
 */
@Composable
private fun ExpandedAlarmCard(
    alarm: Alarm,
    soundTitle: String,
    isUpdating: Boolean,
    onCollapse: () -> Unit,
    onOpenSoundList: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalWakepointColors.current
    var label by remember(alarm.id, alarm.label) { mutableStateOf(alarm.label) }
    var radius by remember(alarm.id, alarm.radiusKm) { mutableStateOf(alarm.radiusKm.toRadiusOption()) }

    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCollapse),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.alarm_card_alias, alarm.label),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(WakepointSize.icon)
                    )
                }
                WakepointSwitch(
                    checked = alarm.isActive,
                    onCheckedChange = onActiveChange,
                    enabled = !isUpdating
                )
            }
            // 카드 안이므로 채움 필드를 쓴다 (DESIGN.md 5절).
            WakepointTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = stringResource(R.string.alarm_location_alias),
                filled = true,
                enabled = !isUpdating
            )
            RadiusSelector(
                options = ALARM_RADIUS_OPTIONS,
                selectedOption = radius,
                onSelected = { radius = it },
                label = stringResource(R.string.alarm_radius_label),
                filled = true
            )
            LabeledSection(label = stringResource(R.string.alarm_sound_label)) {
                AlarmRowGroup {
                    AlarmReadOnlyRow(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        title = soundTitle,
                        subtitle = stringResource(R.string.alarm_sound_hint),
                        trailingIcon = Icons.Rounded.ChevronRight,
                        onClick = onOpenSoundList
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                WakepointButton(
                    text = stringResource(R.string.alarm_save),
                    enabled = !isUpdating,
                    onClick = { onSave(label, radius) },
                    modifier = Modifier.weight(1f)
                )
                WakepointSecondaryButton(
                    text = stringResource(R.string.alarm_delete),
                    icon = Icons.Rounded.DeleteOutline,
                    enabled = !isUpdating,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * B안 별도 수정 화면 (design-handoff `Alarms-edit`).
 * 본문만 스크롤하고 하단 삭제/저장 줄은 고정한다.
 */
@Composable
private fun AlarmEditContent(
    alarm: Alarm,
    soundTitle: String,
    isUpdating: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit = {},
    onOpenSoundList: () -> Unit = {},
    onActiveChange: (Boolean) -> Unit = {},
    onVibrateChange: (Boolean) -> Unit = {},
    onRepeatingChange: (Boolean) -> Unit = {},
    onDelete: () -> Unit = {},
    onSave: (String, String) -> Unit = { _, _ -> }
) {
    val colors = LocalWakepointColors.current
    var label by remember(alarm.id, alarm.label) { mutableStateOf(alarm.label) }
    var radius by remember(alarm.id, alarm.radiusKm) { mutableStateOf(alarm.radiusKm.toRadiusOption()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        WakepointPageHeader(
            title = stringResource(R.string.alarm_edit_title),
            navigation = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(WakepointSize.icon)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.formHorizontal)
                .padding(top = WakepointSpacing.sm, bottom = WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap)
        ) {
            LabeledSection(label = stringResource(R.string.alarm_destination_label)) {
                AlarmRowGroup {
                    AlarmReadOnlyRow(
                        icon = Icons.Rounded.LocationOn,
                        title = alarm.targetAddress.ifBlank {
                            stringResource(R.string.alarm_selected_target_unknown)
                        },
                        subtitle = stringResource(R.string.home_map_selected_location)
                    )
                }
            }
            // 화면 배경 위라 테두리 필드를 쓴다 (DESIGN.md 5절).
            WakepointTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = stringResource(R.string.alarm_location_alias),
                enabled = !isUpdating
            )
            RadiusSelector(
                options = ALARM_RADIUS_OPTIONS,
                selectedOption = radius,
                onSelected = { radius = it },
                label = stringResource(R.string.alarm_radius_label),
                filled = false
            )
            LabeledSection(label = stringResource(R.string.alarm_sound_label)) {
                AlarmRowGroup {
                    AlarmReadOnlyRow(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        title = soundTitle,
                        subtitle = stringResource(R.string.alarm_sound_hint),
                        trailingIcon = Icons.Rounded.ChevronRight,
                        onClick = onOpenSoundList
                    )
                }
            }
            AlarmRowGroup {
                AlarmSwitchRow(
                    icon = Icons.Rounded.Alarm,
                    title = stringResource(R.string.alarm_enable_row),
                    checked = alarm.isActive,
                    enabled = !isUpdating,
                    onCheckedChange = onActiveChange
                )
            }
            // 디자인에는 없지만 기존 설정이라 같은 묶음 행 모양으로 유지한다.
            AlarmRowGroup {
                AlarmSwitchRow(
                    icon = Icons.Rounded.Alarm,
                    title = stringResource(R.string.alarm_vibrate_setting),
                    checked = alarm.vibrate,
                    enabled = !isUpdating,
                    onCheckedChange = onVibrateChange
                )
                WakepointDivider(inset = true)
                AlarmSwitchRow(
                    icon = Icons.Rounded.Alarm,
                    title = stringResource(R.string.alarm_repeating_setting),
                    checked = alarm.repeating,
                    enabled = !isUpdating,
                    onCheckedChange = onRepeatingChange
                )
            }
            if (errorMessage != null) {
                WakepointMessage(text = errorMessage, kind = WakepointMessageKind.Error)
            }
        }
        BottomActionBar(
            secondaryText = stringResource(R.string.alarm_delete),
            secondaryIcon = Icons.Rounded.DeleteOutline,
            primaryText = if (isUpdating) {
                stringResource(R.string.alarm_saving)
            } else {
                stringResource(R.string.alarm_save)
            },
            primaryEnabled = !isUpdating,
            secondaryEnabled = !isUpdating,
            onSecondary = onDelete,
            onPrimary = { onSave(label, radius) }
        )
    }
}

/** 하단 고정 버튼 줄 — 위 구분선 1dp `line`, 보조 1 : 실행 1.4. */
@Composable
private fun BottomActionBar(
    secondaryText: String,
    primaryText: String,
    modifier: Modifier = Modifier,
    secondaryIcon: ImageVector? = null,
    primaryIcon: ImageVector? = null,
    secondaryEnabled: Boolean = true,
    primaryEnabled: Boolean = true,
    secondaryWeight: Float = BOTTOM_SECONDARY_WEIGHT,
    primaryWeight: Float = BOTTOM_PRIMARY_WEIGHT,
    onSecondary: () -> Unit = {},
    onPrimary: () -> Unit = {},
    extraContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        WakepointDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = WakepointSpacing.formHorizontal,
                    vertical = WakepointSpacing.md
                ),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            extraContent?.invoke(this)
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
                WakepointSecondaryButton(
                    text = secondaryText,
                    icon = secondaryIcon,
                    enabled = secondaryEnabled,
                    onClick = onSecondary,
                    modifier = Modifier.weight(secondaryWeight)
                )
                WakepointButton(
                    text = primaryText,
                    icon = primaryIcon,
                    enabled = primaryEnabled,
                    onClick = onPrimary,
                    modifier = Modifier.weight(primaryWeight)
                )
            }
        }
    }
}

/** 화면 위 묶음 행 컨테이너 — 모서리 16, 배경 `fill` (DESIGN.md 2절). */
@Composable
private fun AlarmRowGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(LocalWakepointColors.current.fill),
        content = content
    )
}

/** 묶음 행 — 최소 높이 64, 좌우 16, 아이콘 24 + 제목/부제. */
@Composable
private fun AlarmReadOnlyRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = WakepointSize.soundRowHeight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = WakepointSpacing.lg, vertical = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.text)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
}

/** 묶음 행 + 스위치 — 최소 높이 64, 아이콘 24 + 제목 + 스위치. */
@Composable
private fun AlarmSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = WakepointSize.soundRowHeight)
            .padding(horizontal = WakepointSpacing.lg, vertical = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.text,
            modifier = Modifier.weight(1f)
        )
        WakepointSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/** 섹션 라벨(labelLarge `textSecondary`) + 콘텐츠, 라벨↔콘텐츠 8. */
@Composable
private fun LabeledSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = LocalWakepointColors.current.textSecondary
        )
        content()
    }
}

/** 삭제 확인 다이얼로그 (design-handoff `Alarms-delete`). 빨강을 쓰지 않고 아이콘으로 구분한다. */
@Composable
private fun AlarmDeleteDialog(
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    WakepointConfirmDialog(
        title = stringResource(R.string.alarm_delete_confirm_title),
        message = message,
        cancelText = stringResource(R.string.alarm_cancel),
        confirmText = stringResource(R.string.alarm_delete),
        icon = Icons.Rounded.DeleteOutline,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

/** 선택 모드 상단 바 — 디자인에 없는 기존 기능이라 토큰만 맞춰 유지한다. */
@Composable
private fun AlarmSelectionHeader(
    count: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.headerHeight)
            .padding(horizontal = WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.alarm_selection_close),
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
        Text(
            text = stringResource(R.string.alarm_selection_count, count),
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            modifier = Modifier
                .weight(1f)
                .padding(start = WakepointSpacing.sm)
        )
        IconButton(onClick = onToggleSelectAll) {
            Icon(
                imageVector = Icons.Rounded.SelectAll,
                contentDescription = stringResource(R.string.alarm_select_all),
                tint = if (allSelected) colors.brand else colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
        IconButton(onClick = onDelete, enabled = count > 0) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.alarm_delete_selected),
                tint = if (count > 0) colors.textSecondary else colors.disabledContent,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
}

/** 선택 모드 카드 — 선택되면 `brandSubtle` + `brand` 테두리. */
@Composable
private fun SelectableAlarmCard(
    alarm: Alarm,
    selected: Boolean,
    onToggleSelect: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .clickable(onClick = onToggleSelect),
        shape = WakepointShape.card,
        color = if (selected) colors.brandSubtle else colors.background,
        border = BorderStroke(WakepointSize.border, if (selected) colors.brand else colors.line)
    ) {
        Row(
            modifier = Modifier.padding(WakepointSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
        ) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.alarm_select_item),
                tint = if (selected) colors.brand else colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
            AlarmCardSummaryText(alarm = alarm, modifier = Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// 알람음 (design-handoff `Sounds` · `Sounds-recording`)
// ─────────────────────────────────────────────────────────────────────

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

    SoundListContent(
        uiState = uiState,
        onBack = {
            viewModel.stopPreview()
            onBack()
        },
        onSelect = { viewModel.selectSound(it) },
        onDeleteSound = { deleteTarget = it },
        onSystemPicker = { openSystemSoundPicker() },
        onImport = { requestImportSound() },
        onRecord = { viewModel.openRecordSheet() }
    )

    if (uiState.recordSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRecordSheet() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() },
            containerColor = LocalWakepointColors.current.surfaceElevated,
            scrimColor = LocalWakepointColors.current.dim,
            shape = WakepointShape.sheet
        ) {
            RecordingSheetContent(
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
            icon = Icons.Rounded.DeleteOutline,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteSound(sound)
                deleteTarget = null
            }
        )
    }
}

/** 알람음 목록 본문 (design-handoff `Sounds`) — 한 장의 카드 안에 행 + 구분선. */
@Composable
private fun SoundListContent(
    uiState: SoundListUiState,
    onBack: () -> Unit = {},
    onSelect: (AlarmSound) -> Unit = {},
    onDeleteSound: (AlarmSound) -> Unit = {},
    onSystemPicker: () -> Unit = {},
    onImport: () -> Unit = {},
    onRecord: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    val sounds = uiState.sounds
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        WakepointPageHeader(
            title = stringResource(R.string.sound_list_title),
            navigation = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(WakepointSize.icon)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(top = WakepointSpacing.sm, bottom = WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
        ) {
            uiState.errorMessage?.let { message ->
                WakepointMessage(text = message, kind = WakepointMessageKind.Error)
            }
            if (sounds.isEmpty()) {
                WakepointEmptyState(
                    icon = Icons.Rounded.MusicNote,
                    title = stringResource(R.string.sound_none),
                    description = stringResource(R.string.sound_custom_empty),
                    modifier = Modifier.padding(top = WakepointSpacing.sm)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WakepointShape.card)
                        .background(colors.card)
                ) {
                    sounds.forEachIndexed { index, sound ->
                        key(sound.id) {
                            if (index > 0) WakepointDivider()
                            SoundOptionRow(
                                title = sound.title,
                                subtitle = sound.subtitle,
                                icon = sound.iconVector(),
                                selected = sound.uri == uiState.selectedUri,
                                onClick = { onSelect(sound) },
                                trailing = {
                                    SoundRowTrailing(
                                        selected = sound.uri == uiState.selectedUri,
                                        onDelete = if (sound.kind == SoundKind.Bundled) {
                                            null
                                        } else {
                                            { onDeleteSound(sound) }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        BottomActionBar(
            secondaryText = stringResource(R.string.sound_import),
            secondaryIcon = Icons.Rounded.FileDownload,
            primaryText = stringResource(R.string.sound_record),
            primaryIcon = Icons.Rounded.Mic,
            // `Sounds` 의 하단 두 버튼은 1:1 이다.
            secondaryWeight = 1f,
            primaryWeight = 1f,
            onSecondary = onImport,
            onPrimary = onRecord,
            extraContent = {
                // 디자인에 없는 기존 기능(기기 알람음 선택)이라 같은 줄 위에 보조 버튼으로 둔다.
                WakepointSecondaryButton(
                    text = stringResource(R.string.sound_system),
                    icon = Icons.Rounded.Alarm,
                    onClick = onSystemPicker,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

/** 사운드 행 오른쪽 — 선택 체크 `brand`, 사용자 사운드는 삭제 아이콘. */
@Composable
private fun SoundRowTrailing(selected: Boolean, onDelete: (() -> Unit)?) {
    val colors = LocalWakepointColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(WakepointSize.minTouchTarget)) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.sound_delete),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(WakepointSize.iconSmall)
                )
            }
        }
    }
}

/** 녹음 시트 (design-handoff `Sounds-recording`) — 본문 스크롤 + 하단 고정 버튼. */
@Composable
private fun RecordingSheetContent(
    phase: RecordingPhase,
    elapsedSeconds: Int,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    onRetry: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = WakepointSpacing.formHorizontal,
                    end = WakepointSpacing.formHorizontal,
                    bottom = WakepointSpacing.xl
                ),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
        ) {
            BottomSheetTitle(title = stringResource(R.string.record_title))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WakepointSpacing.lg, bottom = WakepointSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
            ) {
                Text(
                    text = formatElapsed(elapsedSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    color = colors.text
                )
                Text(
                    text = when (phase) {
                        RecordingPhase.Idle -> stringResource(R.string.record_hint)
                        RecordingPhase.Recording -> stringResource(R.string.record_recording)
                        RecordingPhase.Recorded -> stringResource(R.string.record_done)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                WakepointTintButton(
                    text = when (phase) {
                        RecordingPhase.Idle -> stringResource(R.string.record_start)
                        RecordingPhase.Recording -> stringResource(R.string.record_stop)
                        RecordingPhase.Recorded -> stringResource(R.string.record_retry)
                    },
                    onClick = when (phase) {
                        RecordingPhase.Idle -> onStart
                        RecordingPhase.Recording -> onStop
                        RecordingPhase.Recorded -> onRetry
                    },
                    modifier = Modifier.width(RECORD_BUTTON_WIDTH)
                )
            }
        }
        BottomActionBar(
            secondaryText = stringResource(R.string.alarm_cancel),
            primaryText = stringResource(R.string.alarm_save),
            primaryEnabled = phase == RecordingPhase.Recorded,
            onSecondary = onCancel,
            onPrimary = onSave
        )
    }
}

private fun AlarmSound.iconVector(): ImageVector = when (kind) {
    SoundKind.Bundled -> Icons.Rounded.MusicNote
    SoundKind.Recorded -> Icons.Rounded.Mic
    SoundKind.Imported -> Icons.Rounded.MusicNote
    SoundKind.System -> Icons.AutoMirrored.Rounded.VolumeUp
}

private fun formatElapsed(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%02d:%02d".format(minutes, remaining)
}

private fun Double.toRadiusOption(): String {
    return if (this < 1.0) {
        "${(this * 1000).toInt()}m"
    } else {
        "${this.toInt()}km"
    }
}

/** 알람에 저장된 사운드 URI를 사람이 읽을 수 있는 이름으로 변환. 없으면 기본 벨소리. */
@Composable
private fun Alarm.resolveSoundTitle(titles: Map<String, String>): String =
    soundUri?.let { titles[it] } ?: stringResource(R.string.alarm_default_sound)

/** 오디오 파일 선택기에서 허용할 MIME 타입. */
private val AUDIO_MIME_TYPES = arrayOf("audio/*")

// ─────────────────────────────────────────────────────────────────────
// @Preview — design-handoff 의 상태별 화면
// ─────────────────────────────────────────────────────────────────────

@WakepointScreenPreviews
@Composable
private fun AlarmsListPreview() {
    WakepointTheme {
        AlarmsListContent(uiState = PREVIEW_ALARMS_STATE)
    }
}

@WakepointScreenPreviews
@Composable
private fun AlarmsOpenPreview() {
    WakepointTheme {
        AlarmsListContent(
            uiState = PREVIEW_ALARMS_STATE,
            expandedAlarmId = PREVIEW_ALARMS[0].id
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun AlarmsEditPreview() {
    WakepointTheme {
        AlarmEditContent(
            alarm = PREVIEW_ALARMS[0],
            soundTitle = stringResource(R.string.alarm_default_sound)
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun AlarmsEmptyPreview() {
    WakepointTheme {
        AlarmsListContent(uiState = AlarmsUiState(isLoading = false))
    }
}

@WakepointScreenPreviews
@Composable
private fun AlarmsUpdatingPreview() {
    WakepointTheme {
        AlarmsListContent(
            uiState = PREVIEW_ALARMS_STATE.copy(
                updatingAlarmIds = setOf(PREVIEW_ALARMS[0].id),
                errorMessage = "알람을 변경하지 못했습니다. 네트워크를 확인하고 다시 시도해 주세요."
            )
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun AlarmsDeletePreview() {
    WakepointTheme {
        AlarmsListContent(uiState = PREVIEW_ALARMS_STATE)
        AlarmDeleteDialog(
            message = stringResource(R.string.alarm_delete_confirm_message),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun SoundsPreview() {
    WakepointTheme {
        SoundListContent(uiState = PREVIEW_SOUND_STATE)
    }
}

@WakepointScreenPreviews
@Composable
private fun SoundsRecordingPreview() {
    WakepointTheme {
        val colors = LocalWakepointColors.current
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                SoundListContent(uiState = PREVIEW_SOUND_STATE)
            }
            // 시트 컨테이너(모서리 28 · `surfaceElevated` · 드래그 핸들)를 미리보기에서 재현한다.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WakepointShape.sheet)
                    .background(colors.surfaceElevated)
            ) {
                BottomSheetHandle()
                RecordingSheetContent(phase = RecordingPhase.Recording, elapsedSeconds = 7)
            }
        }
    }
}

private val PREVIEW_ALARMS = listOf(
    previewAlarm("1", "회사", "서울 강남구 테헤란로 152", 0.5, true),
    previewAlarm("2", "집 앞 정류장", "서울 마포구 월드컵북로 396", 2.0, true),
    previewAlarm("3", "본가", "경기 수원시 팔달구 효원로 241", 1.0, false),
    previewAlarm("4", "학원", "서울 서초구 강남대로 465", 0.3, false)
)

private val PREVIEW_ALARMS_STATE = AlarmsUiState(isLoading = false, alarms = PREVIEW_ALARMS)

private val PREVIEW_SOUNDS = listOf(
    AlarmSound("s0", "기본 알람음", "시스템 기본", "content://system", SoundKind.System),
    AlarmSound("s1", "부드러운 종", "내장 알람음", "res://bell", SoundKind.Bundled),
    AlarmSound("s2", "지하철 안내음", "내장 알람음", "res://subway", SoundKind.Bundled),
    AlarmSound("s3", "내 녹음 1", "녹음 · 0:07", "file://rec1", SoundKind.Recorded)
)

private val PREVIEW_SOUND_STATE = SoundListUiState(
    sounds = PREVIEW_SOUNDS,
    selectedUri = "content://system"
)

private fun previewAlarm(
    id: String,
    label: String,
    address: String,
    radiusKm: Double,
    active: Boolean
) = Alarm(
    id = id,
    ownerId = "me",
    createdBy = "me",
    label = label,
    targetLat = 37.5,
    targetLng = 127.0,
    targetAddress = address,
    radiusKm = radiusKm,
    isActive = active,
    soundType = SoundType.Default,
    soundUri = null
)
