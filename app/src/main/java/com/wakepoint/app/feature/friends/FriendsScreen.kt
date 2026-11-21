package com.wakepoint.app.feature.friends

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointButtonSize
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointEmptyState
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMessage
import com.wakepoint.app.core.design.WakepointMessageKind
import com.wakepoint.app.core.design.WakepointScreenPreviews
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTintButton
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.UserProfile
import com.wakepoint.app.feature.home.AlarmSetupSheet

// ─────────────────────────────────────────────────────────────────────
// 친구 화면 (design-handoff `Friends-requests` · `-list` · `-empty` · `-error` · `-block`,
// 친구 검색 시트 `FriendSearch-idle` · `-searching` · `-results` · `-none` · `-error`).
// 명세 7절. 디자인 토큰만 쓰고 hex 를 직접 쓰지 않는다.
//
// 개인정보: 알람을 대신 설정해 준 친구에게 상대의 위치 · 도착 여부 · 알람 울림 상태를
// 보여 주는 UI 는 어떤 형태로도 만들지 않는다 (docs/DESTINATION_ALARM_PRIVACY.md).
// ─────────────────────────────────────────────────────────────────────

/** 하단 탭 바가 차지하는 높이(시스템 내비게이션 바 제외). */
private val BOTTOM_TAB_CLEARANCE = 80.dp

// 탭 행의 구조적 치수 (디자인 토큰 대상이 아닌 구조 상수).
private val TAB_INDICATOR_WIDTH = 72.dp
private val TAB_INDICATOR_HEIGHT = 3.dp
private val TAB_BADGE_SIZE = 20.dp

/** 목록 행 아바타 = 48 (DESIGN 의 `Avatar/Initial`). */
private const val ROW_AVATAR_SIZE = 48

/** 시트 검색 버튼 폭 = 104. */
private val SEARCH_BUTTON_WIDTH = 104.dp

/** 선택된 검색 결과 아바타 위 체크 배지 = 20. */
private val SELECT_BADGE_SIZE = 20.dp

private enum class FriendsTab { Requests, Friends }

private enum class FriendConfirmAction {
    Delete,
    Block
}

private data class FriendConfirmTarget(
    val friend: Friend,
    val action: FriendConfirmAction
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onOpenSentAlarms: () -> Unit = {},
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var expandedFriendId by remember { mutableStateOf<String?>(null) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var confirmTarget by remember { mutableStateOf<FriendConfirmTarget?>(null) }
    // 요청이 0건이면 "친구" 탭이 기본 (DESIGN.md 8절). 사용자가 고른 뒤에는 그 선택을 따른다.
    var pickedTab by remember { mutableStateOf<FriendsTab?>(null) }

    LaunchedEffect(uiState.sendAlarmSucceeded) {
        if (uiState.sendAlarmSucceeded) {
            Toast.makeText(
                context,
                context.getString(R.string.alarm_send_success),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.consumeSendAlarmSuccess()
        }
    }

    val requestCount = uiState.requestCount
    val selectedTab = pickedTab
        ?: if (requestCount > 0) FriendsTab.Requests else FriendsTab.Friends

    FriendsContent(
        uiState = uiState,
        selectedTab = selectedTab,
        expandedFriendId = expandedFriendId,
        onSelectTab = { pickedTab = it },
        onOpenSearch = { showSearchSheet = true },
        onRefresh = viewModel::refreshFriends,
        onOpenSentAlarms = onOpenSentAlarms,
        onToggleFriend = { friend ->
            expandedFriendId = if (expandedFriendId == friend.id) null else friend.id
        },
        onAcceptFriend = { viewModel.acceptFriendRequest(it.id) },
        onRejectFriend = { viewModel.rejectFriendRequest(it.id) },
        onCancelFriend = { viewModel.deleteFriend(it.id) },
        onAcceptPermission = { viewModel.acceptAlarmPermission(it.id) },
        onRejectPermission = { viewModel.rejectAlarmPermission(it.id) },
        onSendAlarm = viewModel::openSendAlarm,
        onRequestPermission = {
            viewModel.requestAlarmPermission(it.otherUserId(uiState.currentUserId))
        },
        onDeleteFriend = { confirmTarget = FriendConfirmTarget(it, FriendConfirmAction.Delete) },
        onBlockFriend = { confirmTarget = FriendConfirmTarget(it, FriendConfirmAction.Block) }
    )

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() },
            containerColor = LocalWakepointColors.current.surfaceElevated,
            scrimColor = LocalWakepointColors.current.dim
        ) {
            val inviteLink = stringResource(R.string.invite_link)
            val inviteMessage = if (uiState.currentUserEmail.isBlank()) {
                stringResource(R.string.invite_message_no_email, inviteLink)
            } else {
                stringResource(R.string.invite_message, uiState.currentUserEmail, inviteLink)
            }
            val shareSubject = stringResource(R.string.invite_share_subject)
            val kakaoUnavailable = stringResource(R.string.invite_kakao_unavailable)
            val linkCopied = stringResource(R.string.invite_link_copied)
            FriendSearchSheet(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                selectedUserId = uiState.selectedSearchUserId,
                requestingUserIds = uiState.requestingUserIds,
                isSearching = uiState.isSearching,
                message = uiState.searchMessage,
                onQueryChange = viewModel::updateSearchQuery,
                onSearch = viewModel::searchUsers,
                onSelectResult = viewModel::selectSearchResult,
                onSendRequest = viewModel::sendFriendRequest,
                onShareKakao = {
                    context.shareInviteViaKakao(inviteMessage, shareSubject, kakaoUnavailable)
                },
                onCopyLink = {
                    context.copyInviteLink(inviteLink, linkCopied)
                }
            )
        }
    }

    if (uiState.sendAlarmFriend != null) {
        val friend = uiState.sendAlarmFriend
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSendAlarm() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() },
            containerColor = LocalWakepointColors.current.surfaceElevated,
            scrimColor = LocalWakepointColors.current.dim
        ) {
            // v2 통합: 친구 알람 보내기는 주소/장소/시간 3탭 형태. 주소 탭만 실기능(백엔드 존재)이고
            // 장소·시간 탭은 도메인/영속성 미구현이라 "준비 중"으로 안내한다.
            var sendTab by remember { mutableStateOf(SendAlarmTab.Address) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WakepointSpacing.formHorizontal),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
            ) {
                SendAlarmSegmentedTabs(selected = sendTab, onSelect = { sendTab = it })
                SendAlarmPrivacyNote()
            }
            when (sendTab) {
                SendAlarmTab.Address -> AlarmSetupSheet(
                    title = friend?.let { stringResource(R.string.friends_send_alarm_to, it.nickname) }
                        ?: stringResource(R.string.alarm_send_title),
                    primaryButton = stringResource(R.string.alarm_send),
                    targetAddress = uiState.selectedTargetAddress,
                    label = uiState.sendAlarmLabel,
                    radius = uiState.sendAlarmRadius,
                    soundEnabled = uiState.sendAlarmSoundEnabled,
                    vibrate = uiState.sendAlarmVibrate,
                    isSaving = uiState.isSendingAlarm,
                    errorMessage = uiState.sendAlarmErrorMessage,
                    showAddressSearch = true,
                    addressQuery = uiState.addressQuery,
                    addressResults = uiState.addressResults,
                    isSearchingAddress = uiState.isSearchingAddress,
                    addressMessage = uiState.addressMessage,
                    onAddressQueryChange = viewModel::updateAddressQuery,
                    onAddressSearch = viewModel::searchAddress,
                    onSelectAddress = viewModel::selectAddress,
                    onLabelChange = viewModel::updateSendAlarmLabel,
                    onRadiusChange = viewModel::updateSendAlarmRadius,
                    onSoundEnabledChange = viewModel::updateSendAlarmSoundEnabled,
                    onVibrateChange = viewModel::updateSendAlarmVibrate,
                    onPrimaryClick = {
                        viewModel.sendAlarm(context.getString(R.string.alarm_default_label))
                    },
                    onDismiss = { viewModel.dismissSendAlarm() }
                )
                SendAlarmTab.Place -> SendAlarmPlaceTab(
                    friendName = friend?.nickname ?: stringResource(R.string.friends_title),
                    onDismiss = { viewModel.dismissSendAlarm() }
                )
                SendAlarmTab.Time -> SendAlarmTimeTab(
                    onDismiss = { viewModel.dismissSendAlarm() }
                )
            }
        }
    }

    confirmTarget?.let { target ->
        WakepointConfirmDialog(
            icon = when (target.action) {
                FriendConfirmAction.Delete -> Icons.Rounded.Delete
                FriendConfirmAction.Block -> Icons.Rounded.Block
            },
            title = when (target.action) {
                FriendConfirmAction.Delete ->
                    stringResource(R.string.friends_delete_confirm_title, target.friend.nickname)
                FriendConfirmAction.Block ->
                    stringResource(R.string.friends_block_confirm_title, target.friend.nickname)
            },
            message = when (target.action) {
                FriendConfirmAction.Delete -> stringResource(R.string.friends_delete_confirm_message)
                FriendConfirmAction.Block -> stringResource(R.string.friends_block_confirm_message)
            },
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = when (target.action) {
                FriendConfirmAction.Delete -> stringResource(R.string.alarm_delete)
                FriendConfirmAction.Block -> stringResource(R.string.friends_block)
            },
            onDismiss = { confirmTarget = null },
            onConfirm = {
                when (target.action) {
                    FriendConfirmAction.Delete -> viewModel.deleteFriend(target.friend.id)
                    FriendConfirmAction.Block -> viewModel.blockFriend(target.friend.id)
                }
                if (expandedFriendId == target.friend.id) {
                    expandedFriendId = null
                }
                confirmTarget = null
            }
        )
    }
}

/** 요청 탭 배지에 쓰는 요청 합계 (받은/보낸 친구 요청 + 받은/보낸 알람 권한 요청). */
private val FriendsUiState.requestCount: Int
    get() = receivedFriendRequests.size + sentFriendRequests.size +
        receivedAlarmPermissionRequests.size + sentAlarmPermissionRequests.size

/**
 * 화면 본문 — 헤더(고정) + 검색바(고정) + 탭 행(고정) + 스크롤 목록.
 * ViewModel 없이 그릴 수 있어 상태별 미리보기에 그대로 쓴다.
 */
@Composable
private fun FriendsContent(
    uiState: FriendsUiState,
    selectedTab: FriendsTab,
    expandedFriendId: String?,
    onSelectTab: (FriendsTab) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenSentAlarms: () -> Unit = {},
    onToggleFriend: (Friend) -> Unit = {},
    onAcceptFriend: (Friend) -> Unit = {},
    onRejectFriend: (Friend) -> Unit = {},
    onCancelFriend: (Friend) -> Unit = {},
    onAcceptPermission: (AlarmPermissionRequest) -> Unit = {},
    onRejectPermission: (AlarmPermissionRequest) -> Unit = {},
    onSendAlarm: (Friend) -> Unit = {},
    onRequestPermission: (Friend) -> Unit = {},
    onDeleteFriend: (Friend) -> Unit = {},
    onBlockFriend: (Friend) -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    val requestCount = uiState.requestCount
    val hasAnyContent = requestCount > 0 || uiState.acceptedFriends.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        WakepointHeader(
            action = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
                ) {
                    // v2 STEP3 (3-6): 보낸 알람 목록 진입점 — 디자인에는 없지만 기존 기능을 유지한다.
                    IconButton(onClick = onOpenSentAlarms) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.friends_sent_alarms),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(WakepointSize.iconSmall)
                        )
                    }
                    WakepointTextButton(
                        text = stringResource(R.string.friends_refresh),
                        enabled = !uiState.isRefreshing,
                        onClick = onRefresh
                    )
                    WakepointTintButton(
                        text = stringResource(R.string.friends_add),
                        icon = Icons.Rounded.Add,
                        size = WakepointButtonSize.Small,
                        onClick = onOpenSearch
                    )
                }
            }
        )

        Box(
            modifier = Modifier.padding(
                start = WakepointSpacing.screenHorizontal,
                end = WakepointSpacing.screenHorizontal,
                bottom = WakepointSpacing.sm
            )
        ) {
            FriendsSearchBar(onClick = onOpenSearch)
        }

        if (hasAnyContent) {
            FriendsTabRow(
                selected = selectedTab,
                requestCount = requestCount,
                friendCount = uiState.acceptedFriends.size,
                onSelect = onSelectTab
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(top = WakepointSpacing.md, bottom = BOTTOM_TAB_CLEARANCE + WakepointSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
        ) {
            uiState.errorMessage?.let { message ->
                WakepointMessage(text = message, kind = WakepointMessageKind.Error)
            }

            when {
                uiState.isLoading -> WakepointLoadingPlaceholder()
                !hasAnyContent -> WakepointEmptyState(
                    icon = Icons.Rounded.Groups,
                    title = stringResource(R.string.friends_empty_title),
                    description = stringResource(R.string.friends_empty_description),
                    action = {
                        WakepointButton(
                            text = stringResource(R.string.friends_search),
                            icon = Icons.Rounded.Search,
                            onClick = onOpenSearch,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
                selectedTab == FriendsTab.Requests -> if (requestCount == 0) {
                    // 디자인에 없는 상태(요청 탭을 직접 눌러 들어왔는데 요청이 0건).
                    WakepointMessage(text = stringResource(R.string.friends_requests_empty))
                } else {
                    RequestsTabContent(
                        uiState = uiState,
                        onAcceptFriend = onAcceptFriend,
                        onRejectFriend = onRejectFriend,
                        onCancelFriend = onCancelFriend,
                        onAcceptPermission = onAcceptPermission,
                        onRejectPermission = onRejectPermission
                    )
                }
                uiState.acceptedFriends.isEmpty() ->
                    // 디자인에 없는 상태(요청만 있고 수락된 친구는 0명).
                    WakepointMessage(text = stringResource(R.string.friends_accepted_empty))
                else -> uiState.acceptedFriends.forEach { friend ->
                    key(friend.id) {
                        FriendCard(
                            friend = friend,
                            currentUserId = uiState.currentUserId,
                            expanded = expandedFriendId == friend.id,
                            isUpdating = friend.id in uiState.updatingFriendIds,
                            onClick = { onToggleFriend(friend) },
                            onSendAlarm = { onSendAlarm(friend) },
                            onRequestAlarmPermission = { onRequestPermission(friend) },
                            onDelete = { onDeleteFriend(friend) },
                            onBlock = { onBlockFriend(friend) }
                        )
                    }
                }
            }
        }
    }
}

/** 요청 탭 — 받은/보낸 친구 요청, 받은/보낸 알람 권한 요청 네 묶음. */
@Composable
private fun RequestsTabContent(
    uiState: FriendsUiState,
    onAcceptFriend: (Friend) -> Unit,
    onRejectFriend: (Friend) -> Unit,
    onCancelFriend: (Friend) -> Unit,
    onAcceptPermission: (AlarmPermissionRequest) -> Unit,
    onRejectPermission: (AlarmPermissionRequest) -> Unit
) {
    if (uiState.receivedFriendRequests.isNotEmpty()) {
        SectionTitle(
            text = stringResource(R.string.friends_section_received_requests),
            count = uiState.receivedFriendRequests.size
        )
        uiState.receivedFriendRequests.forEach { friend ->
            key("received-${friend.id}") {
                FriendRequestCard(
                    friend = friend,
                    isIncoming = true,
                    isUpdating = friend.id in uiState.updatingFriendIds,
                    onAccept = { onAcceptFriend(friend) },
                    onReject = { onRejectFriend(friend) },
                    onCancel = { onCancelFriend(friend) }
                )
            }
        }
    }

    if (uiState.sentFriendRequests.isNotEmpty()) {
        SectionTitle(
            text = stringResource(R.string.friends_section_sent_requests),
            count = uiState.sentFriendRequests.size
        )
        uiState.sentFriendRequests.forEach { friend ->
            key("sent-${friend.id}") {
                FriendRequestCard(
                    friend = friend,
                    isIncoming = false,
                    isUpdating = friend.id in uiState.updatingFriendIds,
                    onCancel = { onCancelFriend(friend) }
                )
            }
        }
    }

    if (uiState.receivedAlarmPermissionRequests.isNotEmpty()) {
        SectionTitle(
            text = stringResource(R.string.friends_section_received_permissions),
            count = uiState.receivedAlarmPermissionRequests.size
        )
        uiState.receivedAlarmPermissionRequests.forEach { request ->
            key("permission-received-${request.id}") {
                AlarmPermissionRequestCard(
                    request = request,
                    currentUserId = uiState.currentUserId,
                    isUpdating = request.id in uiState.updatingPermissionIds,
                    onAccept = { onAcceptPermission(request) },
                    onReject = { onRejectPermission(request) }
                )
            }
        }
    }

    if (uiState.sentAlarmPermissionRequests.isNotEmpty()) {
        SectionTitle(
            text = stringResource(R.string.friends_section_sent_permissions),
            count = uiState.sentAlarmPermissionRequests.size
        )
        uiState.sentAlarmPermissionRequests.forEach { request ->
            key("permission-sent-${request.id}") {
                AlarmPermissionRequestCard(
                    request = request,
                    currentUserId = uiState.currentUserId,
                    isUpdating = request.id in uiState.updatingPermissionIds
                )
            }
        }
    }
}

/** 읽기 전용 검색바 — 높이 56, 완전 원형, `fill`. 누르면 친구 검색 시트가 열린다. */
@Composable
private fun FriendsSearchBar(onClick: () -> Unit) {
    val colors = LocalWakepointColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(WakepointSize.searchBarHeight),
        shape = WakepointShape.full,
        color = colors.fill
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = WakepointSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
            Text(
                text = stringResource(R.string.friends_search_bar_placeholder),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 요청 · 친구 탭 행 — 높이 48, 선택 `brand` + 72×3 인디케이터, 아래 1dp `line`. */
@Composable
private fun FriendsTabRow(
    selected: FriendsTab,
    requestCount: Int,
    friendCount: Int,
    onSelect: (FriendsTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            FriendsTabItem(
                label = stringResource(R.string.friends_request),
                count = requestCount,
                selected = selected == FriendsTab.Requests,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(FriendsTab.Requests) }
            )
            FriendsTabItem(
                label = stringResource(R.string.friends_title),
                count = friendCount,
                selected = selected == FriendsTab.Friends,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(FriendsTab.Friends) }
            )
        }
        WakepointDivider()
    }
}

@Composable
private fun FriendsTabItem(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .height(WakepointSize.minTouchTarget)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) colors.brand else colors.inactive
            )
            if (count > 0) {
                TabBadge(count = count, selected = selected)
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(TAB_INDICATOR_WIDTH)
                    .height(TAB_INDICATOR_HEIGHT)
                    .clip(
                        RoundedCornerShape(
                            topStart = TAB_INDICATOR_HEIGHT,
                            topEnd = TAB_INDICATOR_HEIGHT,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(colors.brand)
            )
        }
    }
}

/** 탭 배지 — 지름 20, 선택 `brand`+`onBrand`, 비선택 `fill`+`textSecondary`, labelSmall. */
@Composable
private fun TabBadge(count: Int, selected: Boolean) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = TAB_BADGE_SIZE, minHeight = TAB_BADGE_SIZE)
            .clip(CircleShape)
            .background(if (selected) colors.brand else colors.fill)
            .padding(horizontal = WakepointSpacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onBrand else colors.textSecondary
        )
    }
}

/** 섹션 제목 — titleMedium + 개수 labelLarge `textSecondary`. */
@Composable
private fun SectionTitle(text: String, count: Int) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier.padding(top = WakepointSpacing.sm, start = WakepointSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = colors.text
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary
        )
    }
}

/** 친구 요청 카드 — 테두리 카드. 받은 요청은 수락/거절, 보낸 요청은 요청 취소. */
@Composable
private fun FriendRequestCard(
    friend: Friend,
    isIncoming: Boolean,
    isUpdating: Boolean,
    onCancel: () -> Unit,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    WakepointCard(filled = false) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            FriendListItem(
                name = friend.nickname,
                subtitle = friend.email,
                pill = {
                    FriendStatusPill(
                        status = FriendStatus.Pending,
                        currentUserId = if (isIncoming) "" else friend.userId,
                        userId = friend.userId
                    )
                }
            )
            if (isIncoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointButton(
                        text = stringResource(R.string.friends_accept),
                        enabled = !isUpdating,
                        onClick = onAccept,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_reject),
                        enabled = !isUpdating,
                        onClick = onReject,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                WakepointSecondaryButton(
                    text = stringResource(R.string.friends_cancel_request),
                    enabled = !isUpdating,
                    onClick = onCancel,
                    size = WakepointButtonSize.Small,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** 알람 권한 요청 카드 — 받은 요청만 수락/거절 버튼이 붙는다. */
@Composable
private fun AlarmPermissionRequestCard(
    request: AlarmPermissionRequest,
    currentUserId: String,
    isUpdating: Boolean,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    val isIncoming = request.targetId == currentUserId
    val profile = if (isIncoming) request.requesterProfile else request.targetProfile
    WakepointCard(filled = false) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            FriendListItem(
                name = profile?.nickname ?: stringResource(R.string.friends_unknown_user),
                subtitle = if (isIncoming) {
                    stringResource(R.string.friends_permission_incoming_subtitle)
                } else {
                    stringResource(R.string.friends_permission_outgoing_subtitle)
                },
                caption = stringResource(R.string.friends_permission_no_expiry),
                pill = { PermissionStatusPill(status = request.status) }
            )
            if (isIncoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointButton(
                        text = stringResource(R.string.friends_accept),
                        enabled = !isUpdating,
                        onClick = onAccept,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_reject),
                        enabled = !isUpdating,
                        onClick = onReject,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 친구 카드 — 접힌 상태는 이름 · 이메일 · 알람 권한 한 줄 + "친구" pill.
 * 펼치면 구분선 아래로 알람 보내기 · 차단 · 삭제, 그리고 알람 권한 버튼.
 * 상대의 위치 · 도착 여부 · 울림 상태는 어떤 줄에도 표시하지 않는다.
 */
@Composable
private fun FriendCard(
    friend: Friend,
    currentUserId: String,
    expanded: Boolean,
    isUpdating: Boolean,
    onClick: () -> Unit,
    onSendAlarm: () -> Unit,
    onRequestAlarmPermission: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit
) {
    WakepointCard(filled = false, modifier = Modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            FriendListItem(
                name = friend.nickname,
                subtitle = friend.email,
                caption = friend.permissionStatus.lineText(),
                captionIcon = friend.permissionStatus.lineIcon(),
                pill = {
                    FriendStatusPill(
                        status = friend.status,
                        currentUserId = currentUserId,
                        userId = friend.userId
                    )
                }
            )

            if (expanded) {
                WakepointDivider()
                val canSendAlarm = friend.status == FriendStatus.Accepted &&
                    friend.permissionStatus == PermissionStatus.Accepted
                val canRequestPermission = friend.status == FriendStatus.Accepted &&
                    friend.permissionStatus == PermissionStatus.None
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointButton(
                        text = stringResource(R.string.friends_send_alarm),
                        icon = Icons.AutoMirrored.Rounded.Send,
                        enabled = canSendAlarm && !isUpdating,
                        onClick = onSendAlarm,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1.5f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_block),
                        icon = Icons.Rounded.Block,
                        enabled = !isUpdating && friend.status != FriendStatus.Blocked,
                        onClick = onBlock,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.alarm_delete),
                        icon = Icons.Rounded.Delete,
                        enabled = !isUpdating,
                        onClick = onDelete,
                        size = WakepointButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                }
                WakepointTintButton(
                    text = when (friend.permissionStatus) {
                        PermissionStatus.Accepted -> stringResource(R.string.friends_permission_granted)
                        PermissionStatus.Pending -> stringResource(R.string.permission_pending)
                        else -> stringResource(R.string.friends_permission_request)
                    },
                    icon = when (friend.permissionStatus) {
                        PermissionStatus.Accepted -> Icons.Rounded.Check
                        PermissionStatus.Pending -> Icons.Rounded.Schedule
                        else -> Icons.Rounded.Lock
                    },
                    enabled = canRequestPermission && !isUpdating,
                    onClick = onRequestAlarmPermission,
                    size = WakepointButtonSize.Small,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 카드 안 공통 행 — 아바타 48 + (이름 · 보조 · 캡션) + 오른쪽 상태 pill.
 * 캡션은 알람 권한 상태 같은 "관계" 정보만 담는다. 위치 · 도착 여부는 넣지 않는다.
 */
@Composable
private fun FriendListItem(
    name: String,
    subtitle: String,
    caption: String? = null,
    captionIcon: ImageVector? = null,
    pill: @Composable () -> Unit
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
    ) {
        WakepointAvatar(name = name, size = ROW_AVATAR_SIZE)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (caption != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
                ) {
                    if (captionIcon != null) {
                        Icon(
                            imageVector = captionIcon,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(WakepointSize.iconPill)
                        )
                    }
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        pill()
    }
}

/** v2 통합: 친구 알람 보내기 방식 3탭. 주소만 실기능, 장소/시간은 백엔드 준비 후. */
private enum class SendAlarmTab(@StringRes val labelRes: Int) {
    Address(R.string.friends_send_tab_address),
    Place(R.string.friends_send_tab_place),
    Time(R.string.friends_send_tab_time)
}

@Composable
private fun SendAlarmSegmentedTabs(
    selected: SendAlarmTab,
    onSelect: (SendAlarmTab) -> Unit
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.message)
            .background(colors.surfaceElevatedSecondary)
            .padding(WakepointSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        SendAlarmTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(WakepointSize.chipHeight)
                    .clip(WakepointShape.message)
                    .background(if (isSelected) colors.brandSubtle else Color.Transparent)
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(tab.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) colors.brand else colors.textSecondary
                )
            }
        }
    }
}

/** v2 정책 안내: 알람을 보내도 상대의 위치/도착 여부는 볼 수 없음. */
@Composable
private fun SendAlarmPrivacyNote() {
    val colors = LocalWakepointColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(WakepointSize.iconPill)
        )
        Text(
            text = stringResource(R.string.friends_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary
        )
    }
}

/**
 * v2 STEP3 (3-3, 3c) — 장소 탭. 친구의 공개 장소 중에서 골라 보내는 탭.
 * 공개 장소는 서버 동기화 미구현이라 현재는 항상 빈 상태(3c)가 사실이다 —
 * 원격 스키마가 생기면 [friendPlaces] 에 목록을 연결한다.
 */
@Composable
private fun SendAlarmPlaceTab(
    friendName: String,
    onDismiss: () -> Unit,
    friendPlaces: List<String> = emptyList()
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = WakepointSpacing.formHorizontal)
            .padding(top = WakepointSpacing.lg, bottom = WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        BottomSheetTitle(title = stringResource(R.string.friends_place_alarm_title))
        if (friendPlaces.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = WakepointSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
            ) {
                Text(
                    text = stringResource(R.string.friends_place_empty_title, friendName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.text
                )
                Text(
                    text = stringResource(R.string.friends_place_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        WakepointSecondaryButton(
            text = stringResource(R.string.alarm_cancel),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val SEND_TIME_PRESETS = listOf(
    R.string.friends_time_preset_hour,
    R.string.friends_time_preset_tonight,
    R.string.friends_time_preset_tomorrow,
    R.string.friends_time_preset_custom
)

/**
 * v2 STEP3 (3-4) — 시간 탭. 정한 시각에 친구 기기에서 울리는 일회성 알람.
 * 전송 스키마(서버 time_alarms)가 아직 없어 스펙 UI만 배선하고 보내기는 막는다(3-5 안내).
 */
@Composable
private fun SendAlarmTimeTab(
    onDismiss: () -> Unit
) {
    val colors = LocalWakepointColors.current
    var preset by remember { mutableStateOf(SEND_TIME_PRESETS.first()) }
    var name by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = WakepointSpacing.formHorizontal)
            .padding(top = WakepointSpacing.lg, bottom = WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        BottomSheetTitle(title = stringResource(R.string.friends_time_alarm_title))
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.labelFieldGap)) {
            Text(
                text = stringResource(R.string.friends_time_when_label),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    SEND_TIME_PRESETS.take(2).forEach { p ->
                        SendTimeChip(stringResource(p), preset == p, Modifier.weight(1f)) { preset = p }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    SEND_TIME_PRESETS.drop(2).forEach { p ->
                        SendTimeChip(stringResource(p), preset == p, Modifier.weight(1f)) { preset = p }
                    }
                }
            }
        }
        WakepointTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.friends_time_name_placeholder),
            filled = true
        )
        // 3-5: 수신 측 미지원 안내(서버 전송 스키마 준비 전까지 보내기 차단)
        WakepointMessage(
            text = stringResource(R.string.friends_time_unavailable),
            kind = WakepointMessageKind.InfoOnSheet
        )
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointSecondaryButton(
                text = stringResource(R.string.alarm_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.friends_send),
                enabled = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SendTimeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .height(WakepointSize.chipHeight)
            .clip(WakepointShape.full)
            .background(if (selected) colors.brandSubtle else Color.Transparent)
            .border(
                width = WakepointSize.border,
                color = if (selected) colors.brand else colors.line,
                shape = WakepointShape.full
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.brand else colors.text
        )
    }
}

/**
 * 친구 검색 시트 (design-handoff `FriendSearch-idle` · `-searching` · `-results` · `-none` · `-error`).
 * 시트 안이므로 채움 필드와 `surfaceElevatedSecondary` 안내를 쓴다.
 */
@Composable
private fun FriendSearchSheet(
    query: String,
    results: List<FriendSearchResult>,
    selectedUserId: String?,
    requestingUserIds: Set<String>,
    isSearching: Boolean,
    message: String?,
    errorMessage: String? = null,
    onQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onSelectResult: (String) -> Unit = {},
    onSendRequest: (String) -> Unit = {},
    onShareKakao: () -> Unit = {},
    onCopyLink: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = WakepointSpacing.formHorizontal,
                end = WakepointSpacing.formHorizontal,
                bottom = WakepointSpacing.xxxl
            ),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        BottomSheetTitle(title = stringResource(R.string.friends_search))
        Row(
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            WakepointTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.friends_search_placeholder),
                leadingIcon = Icons.Rounded.Search,
                supportingText = if (query.isBlank()) stringResource(R.string.friends_search_support) else null,
                filled = true,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = if (isSearching) {
                    stringResource(R.string.friends_searching)
                } else {
                    stringResource(R.string.place_search)
                },
                enabled = !isSearching,
                onClick = onSearch,
                modifier = Modifier.width(SEARCH_BUTTON_WIDTH)
            )
        }

        when {
            errorMessage != null -> WakepointMessage(
                text = errorMessage,
                kind = WakepointMessageKind.Error
            )
            message != null -> WakepointMessage(
                text = message,
                kind = WakepointMessageKind.InfoOnSheet
            )
        }

        if (results.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)) {
                results.forEach { result ->
                    key(result.profile.id) {
                        FriendSearchResultRow(
                            result = result,
                            selected = selectedUserId == result.profile.id,
                            requesting = result.profile.id in requestingUserIds,
                            onClick = { onSelectResult(result.profile.id) },
                            onSendRequest = { onSendRequest(result.profile.id) }
                        )
                    }
                }
            }
        }

        // 디자인에는 없지만 기존 초대 기능을 유지한다 (카카오 공유 · 링크 복사).
        InviteSection(onShareKakao = onShareKakao, onCopyLink = onCopyLink)
    }
}

/** 초대 묶음 — 시트 안이므로 `surfaceElevatedSecondary` 컨테이너. */
@Composable
private fun InviteSection(
    onShareKakao: () -> Unit,
    onCopyLink: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(colors.surfaceElevatedSecondary)
            .padding(WakepointSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
    ) {
        Text(
            text = stringResource(R.string.invite_section_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.text
        )
        Text(
            text = stringResource(R.string.invite_section_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointTintButton(
                text = stringResource(R.string.invite_share_kakao),
                icon = Icons.Rounded.Share,
                onClick = onShareKakao,
                size = WakepointButtonSize.Small,
                modifier = Modifier.weight(1f)
            )
            WakepointSecondaryButton(
                text = stringResource(R.string.invite_copy_link),
                icon = Icons.Rounded.Link,
                onClick = onCopyLink,
                size = WakepointButtonSize.Small,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 검색 결과 행 — 모서리 16, 선택되면 배경 `brandSubtle` + 아바타 위 체크 배지.
 * 이미 친구면 "친구" pill, 아니면 "요청" 채움 버튼(선택된 행에서만 활성).
 */
@Composable
private fun FriendSearchResultRow(
    result: FriendSearchResult,
    selected: Boolean,
    requesting: Boolean,
    onClick: () -> Unit,
    onSendRequest: () -> Unit
) {
    val colors = LocalWakepointColors.current
    val status = result.existingFriendStatus
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.card)
            .background(if (selected) colors.brandSubtle else Color.Transparent)
            .clickable(enabled = status == null, onClick = onClick)
            .padding(WakepointSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
    ) {
        Box {
            WakepointAvatar(name = result.profile.nickname, size = ROW_AVATAR_SIZE)
            if (selected) {
                SelectedCheckBadge()
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.profile.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (status != null) {
            FriendStatusPill(status = status)
        } else {
            WakepointButton(
                text = if (requesting) {
                    stringResource(R.string.friends_requesting)
                } else {
                    stringResource(R.string.friends_request)
                },
                enabled = selected && !requesting,
                onClick = onSendRequest,
                size = WakepointButtonSize.Small
            )
        }
    }
}

@Composable
private fun BoxScope.SelectedCheckBadge() {
    val colors = LocalWakepointColors.current
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(SELECT_BADGE_SIZE)
            .clip(CircleShape)
            .background(colors.brand),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = colors.onBrand,
            modifier = Modifier.size(WakepointSize.iconPill)
        )
    }
}

private const val KAKAO_TALK_PACKAGE = "com.kakao.talk"

/** 카카오톡을 지정해 초대 메시지를 공유하고, 미설치 시 일반 공유 시트로 폴백한다. */
private fun Context.shareInviteViaKakao(
    message: String,
    subject: String,
    kakaoUnavailable: String
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val kakaoIntent = Intent(sendIntent).setPackage(KAKAO_TALK_PACKAGE)
    try {
        startActivity(kakaoIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, kakaoUnavailable, Toast.LENGTH_SHORT).show()
        startActivity(Intent.createChooser(sendIntent, subject))
    }
}

/** 초대 링크를 클립보드에 복사한다. */
private fun Context.copyInviteLink(link: String, copiedMessage: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("wakepoint_invite", link))
    Toast.makeText(this, copiedMessage, Toast.LENGTH_SHORT).show()
}

@Composable
private fun PermissionStatus.lineText(): String = stringResource(
    when (this) {
        PermissionStatus.None -> R.string.friends_permission_line_none
        PermissionStatus.Pending -> R.string.friends_permission_line_pending
        PermissionStatus.Accepted -> R.string.friends_permission_line_accepted
        PermissionStatus.Rejected -> R.string.friends_permission_line_rejected
    }
)

private fun PermissionStatus.lineIcon(): ImageVector? = when (this) {
    PermissionStatus.None -> null
    PermissionStatus.Pending -> Icons.Rounded.Schedule
    PermissionStatus.Accepted -> Icons.Rounded.Check
    PermissionStatus.Rejected -> Icons.Rounded.Block
}

/** 알람 권한 pill — 승인 완료 / 대기 진행 / 거절 부정 / 권한 없음 중립 (DESIGN.md 5절). */
@Composable
private fun PermissionStatusPill(status: PermissionStatus) {
    when (status) {
        PermissionStatus.None -> NeutralPill(text = stringResource(R.string.friends_permission_none))
        PermissionStatus.Pending -> ProgressPill(text = stringResource(R.string.friends_pill_pending))
        PermissionStatus.Accepted -> DonePill(text = stringResource(R.string.friends_pill_approved))
        PermissionStatus.Rejected -> NegativePill(text = stringResource(R.string.friends_reject))
    }
}

/** 친구 관계 pill — 친구 완료 / 요청 진행 / 거절됨 · 차단됨 부정. */
@Composable
private fun FriendStatusPill(
    status: FriendStatus,
    currentUserId: String = "",
    userId: String = ""
) {
    val isOutgoingPending = status == FriendStatus.Pending &&
        currentUserId.isNotBlank() && currentUserId == userId
    when (status) {
        FriendStatus.Accepted -> DonePill(text = stringResource(R.string.friends_title))
        FriendStatus.Pending -> ProgressPill(
            text = if (isOutgoingPending) {
                stringResource(R.string.friends_requesting)
            } else {
                stringResource(R.string.friends_request_received)
            }
        )
        FriendStatus.Rejected -> NegativePill(text = stringResource(R.string.permission_rejected))
        FriendStatus.Blocked -> NegativePill(text = stringResource(R.string.friends_blocked))
    }
}

/** 완료 pill — `positiveSurface` + `positive` + 체크. */
@Composable
private fun DonePill(text: String) {
    val colors = LocalWakepointColors.current
    StatusPill(
        text = text,
        color = colors.positive,
        background = colors.positiveSurface,
        icon = Icons.Rounded.Check
    )
}

/** 진행 pill — `fill` + `text` + 시계. */
@Composable
private fun ProgressPill(text: String) {
    val colors = LocalWakepointColors.current
    StatusPill(
        text = text,
        color = colors.text,
        background = colors.fill,
        icon = Icons.Rounded.Schedule
    )
}

/** 부정 pill — 테두리 `line` + `text` + 금지 아이콘. 빨강을 쓰지 않는다. */
@Composable
private fun NegativePill(text: String) {
    val colors = LocalWakepointColors.current
    StatusPill(
        text = text,
        color = colors.text,
        border = colors.line,
        icon = Icons.Rounded.Block
    )
}

/** 중립 pill — `fillWeak` + `textSecondary`. */
@Composable
private fun NeutralPill(text: String) {
    val colors = LocalWakepointColors.current
    StatusPill(
        text = text,
        color = colors.textSecondary,
        background = colors.fillWeak
    )
}

private fun Friend.otherUserId(currentUserId: String): String {
    return if (friendId == currentUserId) userId else friendId
}

// ─────────────────────────────────────────────────────────────────────
// @Preview — design-handoff 의 상태별 화면
// ─────────────────────────────────────────────────────────────────────

private const val PREVIEW_ME = "me"

private fun previewProfile(id: String, nickname: String, email: String) = UserProfile(
    id = id,
    email = email,
    nickname = nickname,
    avatarUrl = null,
    pushToken = null
)

private fun previewFriend(
    id: String,
    nickname: String,
    email: String,
    status: FriendStatus = FriendStatus.Accepted,
    permission: PermissionStatus = PermissionStatus.None,
    outgoing: Boolean = false
) = Friend(
    id = id,
    userId = if (outgoing) PREVIEW_ME else id,
    friendId = if (outgoing) id else PREVIEW_ME,
    nickname = nickname,
    email = email,
    status = status,
    permissionStatus = permission
)

private val PREVIEW_FRIENDS = listOf(
    previewFriend("1", "이지호", "jiho@example.com", permission = PermissionStatus.Accepted),
    previewFriend("2", "박서연", "seoyeon@example.com", permission = PermissionStatus.None),
    previewFriend("3", "최하은", "haeun@example.com", permission = PermissionStatus.Pending),
    previewFriend("4", "정민재", "minjae@example.com", permission = PermissionStatus.Rejected)
)

private val PREVIEW_REQUESTS = listOf(
    previewFriend("10", "박서연", "seoyeon@example.com", status = FriendStatus.Pending),
    previewFriend("11", "김도윤", "doyun@example.com", status = FriendStatus.Pending, outgoing = true)
)

private val PREVIEW_PERMISSION_REQUESTS = listOf(
    AlarmPermissionRequest(
        id = "p1",
        requesterId = "1",
        targetId = PREVIEW_ME,
        status = PermissionStatus.Pending,
        requesterProfile = previewProfile("1", "이지호", "jiho@example.com"),
        targetProfile = null,
        expiresAt = null,
        createdAt = null
    ),
    AlarmPermissionRequest(
        id = "p2",
        requesterId = PREVIEW_ME,
        targetId = "3",
        status = PermissionStatus.Pending,
        requesterProfile = null,
        targetProfile = previewProfile("3", "최하은", "haeun@example.com"),
        expiresAt = null,
        createdAt = null
    )
)

private val PREVIEW_SEARCH_RESULTS = listOf(
    FriendSearchResult(previewProfile("s1", "윤서연", "yoon.sy@example.com"), null),
    FriendSearchResult(previewProfile("s2", "한서연", "hsy@example.com"), null),
    FriendSearchResult(previewProfile("s3", "박서연", "seoyeon@example.com"), FriendStatus.Accepted)
)

private fun previewState(
    friends: List<Friend> = emptyList(),
    permissionRequests: List<AlarmPermissionRequest> = emptyList(),
    errorMessage: String? = null
) = FriendsUiState(
    isLoading = false,
    friends = friends,
    alarmPermissionRequests = permissionRequests,
    currentUserId = PREVIEW_ME,
    errorMessage = errorMessage
)

@WakepointScreenPreviews
@Composable
private fun FriendsRequestsPreview() {
    WakepointTheme {
        FriendsContent(
            uiState = previewState(
                friends = PREVIEW_FRIENDS + PREVIEW_REQUESTS,
                permissionRequests = PREVIEW_PERMISSION_REQUESTS
            ),
            selectedTab = FriendsTab.Requests,
            expandedFriendId = null
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendsListPreview() {
    WakepointTheme {
        FriendsContent(
            uiState = previewState(
                friends = PREVIEW_FRIENDS + PREVIEW_REQUESTS,
                permissionRequests = PREVIEW_PERMISSION_REQUESTS
            ),
            selectedTab = FriendsTab.Friends,
            expandedFriendId = "1"
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendsEmptyPreview() {
    WakepointTheme {
        FriendsContent(
            uiState = previewState(),
            selectedTab = FriendsTab.Friends,
            expandedFriendId = null
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendsErrorPreview() {
    WakepointTheme {
        FriendsContent(
            uiState = previewState(
                friends = PREVIEW_FRIENDS + PREVIEW_REQUESTS,
                permissionRequests = PREVIEW_PERMISSION_REQUESTS,
                errorMessage = "친구 목록을 불러오지 못했습니다. 새로고침을 눌러 주세요."
            ),
            selectedTab = FriendsTab.Friends,
            expandedFriendId = "1"
        )
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendsBlockPreview() {
    WakepointTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FriendsContent(
                uiState = previewState(
                    friends = PREVIEW_FRIENDS + PREVIEW_REQUESTS,
                    permissionRequests = PREVIEW_PERMISSION_REQUESTS
                ),
                selectedTab = FriendsTab.Friends,
                expandedFriendId = "1"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalWakepointColors.current.dim)
            )
            WakepointConfirmDialog(
                icon = Icons.Rounded.Block,
                title = "이지호 님을 차단할까요?",
                message = "차단하면 서로 친구 요청과 알람 권한 요청을 보낼 수 없습니다.",
                cancelText = stringResource(R.string.alarm_cancel),
                confirmText = "차단",
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendSearchIdlePreview() {
    WakepointTheme { PreviewSheetSurface { PreviewFriendSearchSheet() } }
}

@WakepointScreenPreviews
@Composable
private fun FriendSearchSearchingPreview() {
    WakepointTheme {
        PreviewSheetSurface { PreviewFriendSearchSheet(query = "서연", isSearching = true) }
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendSearchResultsPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            PreviewFriendSearchSheet(
                query = "서연",
                results = PREVIEW_SEARCH_RESULTS,
                selectedUserId = "s1"
            )
        }
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendSearchNonePreview() {
    WakepointTheme {
        PreviewSheetSurface {
            PreviewFriendSearchSheet(
                query = "zz",
                message = "검색 결과가 없습니다. 닉네임이나 이메일을 다시 확인해 주세요."
            )
        }
    }
}

@WakepointScreenPreviews
@Composable
private fun FriendSearchErrorPreview() {
    WakepointTheme {
        PreviewSheetSurface {
            PreviewFriendSearchSheet(
                query = "서연",
                errorMessage = "검색하지 못했습니다. 네트워크를 확인하고 다시 검색해 주세요."
            )
        }
    }
}

/** 시트 컨테이너(모서리 28 · `surfaceElevated` · 드래그 핸들)를 미리보기에서 재현한다. */
@Composable
private fun PreviewSheetSurface(content: @Composable () -> Unit) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.dim),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WakepointShape.sheet)
                .background(colors.surfaceElevated)
        ) {
            BottomSheetHandle()
            content()
        }
    }
}

@Composable
private fun PreviewFriendSearchSheet(
    query: String = "",
    results: List<FriendSearchResult> = emptyList(),
    selectedUserId: String? = null,
    isSearching: Boolean = false,
    message: String? = null,
    errorMessage: String? = null
) {
    FriendSearchSheet(
        query = query,
        results = results,
        selectedUserId = selectedUserId,
        requestingUserIds = emptySet(),
        isSearching = isSearching,
        message = message,
        errorMessage = errorMessage
    )
}
