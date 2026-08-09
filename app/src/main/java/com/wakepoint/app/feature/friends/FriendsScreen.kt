package com.wakepoint.app.feature.friends

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointCircleIconButton
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDangerLight
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointField
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointPrimaryLight
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.UserProfile
import com.wakepoint.app.feature.home.AlarmSetupSheet

/** 리스트/FAB가 떠 있는 탭 바에 가리지 않도록 확보하는 구조적 하단 여백. */
private val BOTTOM_TAB_CLEARANCE = 92.dp

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
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var expandedFriendId by remember { mutableStateOf<String?>(null) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var confirmTarget by remember { mutableStateOf<FriendConfirmTarget?>(null) }

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

    uiState.errorMessage?.let { message ->
        WakepointMessageDialog(message = message, onDismiss = viewModel::clearError)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground)
    ) {
        Column {
            WakepointPageHeader(
                title = stringResource(R.string.friends_title),
                subtitle = stringResource(R.string.friends_subtitle),
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                        WakepointCircleIconButton(
                            icon = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.friends_refresh),
                            onClick = viewModel::refreshFriends
                        )
                        WakepointCircleIconButton(
                            icon = Icons.Rounded.PersonAdd,
                            contentDescription = stringResource(R.string.friends_add),
                            onClick = { showSearchSheet = true }
                        )
                    }
                }
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = WakepointSpacing.screenHorizontal)
                    .padding(top = WakepointSpacing.sectionTitleGap, bottom = BOTTOM_TAB_CLEARANCE + WakepointSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
            ) {
                WakepointTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = stringResource(R.string.friends_search),
                    leadingIcon = Icons.Rounded.Search,
                    readOnly = true,
                    modifier = Modifier.clickable { showSearchSheet = true }
                )

                val hasFriendContent = uiState.acceptedFriends.isNotEmpty() ||
                    uiState.receivedFriendRequests.isNotEmpty() ||
                    uiState.sentFriendRequests.isNotEmpty() ||
                    uiState.receivedAlarmPermissionRequests.isNotEmpty() ||
                    uiState.sentAlarmPermissionRequests.isNotEmpty()

                if (!hasFriendContent) {
                    EmptyFriendsCard(onAddFriend = { showSearchSheet = true })
                } else {
                    if (uiState.receivedFriendRequests.isNotEmpty()) {
                        SectionTitle(text = "받은 친구 요청")
                        uiState.receivedFriendRequests.forEach { friend ->
                            key("received-${friend.id}") {
                                FriendRequestCard(
                                    friend = friend,
                                    isIncoming = true,
                                    isUpdating = friend.id in uiState.updatingFriendIds,
                                    onAccept = { viewModel.acceptFriendRequest(friend.id) },
                                    onReject = { viewModel.rejectFriendRequest(friend.id) },
                                    onCancel = { viewModel.deleteFriend(friend.id) }
                                )
                            }
                        }
                    }

                    if (uiState.sentFriendRequests.isNotEmpty()) {
                        SectionTitle(text = "보낸 친구 요청")
                        uiState.sentFriendRequests.forEach { friend ->
                            key("sent-${friend.id}") {
                                FriendRequestCard(
                                    friend = friend,
                                    isIncoming = false,
                                    isUpdating = friend.id in uiState.updatingFriendIds,
                                    onCancel = { viewModel.deleteFriend(friend.id) }
                                )
                            }
                        }
                    }

                    if (uiState.receivedAlarmPermissionRequests.isNotEmpty()) {
                        SectionTitle(text = "받은 알람 권한 요청")
                        uiState.receivedAlarmPermissionRequests.forEach { request ->
                            key("permission-received-${request.id}") {
                                AlarmPermissionRequestCard(
                                    request = request,
                                    currentUserId = uiState.currentUserId,
                                    isUpdating = request.id in uiState.updatingPermissionIds,
                                    onAccept = { viewModel.acceptAlarmPermission(request.id) },
                                    onReject = { viewModel.rejectAlarmPermission(request.id) }
                                )
                            }
                        }
                    }

                    if (uiState.sentAlarmPermissionRequests.isNotEmpty()) {
                        SectionTitle(text = "보낸 알람 권한 요청")
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

                    if (uiState.acceptedFriends.isNotEmpty()) {
                        SectionTitle(text = stringResource(R.string.friends_section))
                    }
                    uiState.acceptedFriends.forEach { friend ->
                        key(friend.id) {
                            FriendCard(
                                friend = friend,
                                currentUserId = uiState.currentUserId,
                                expanded = expandedFriendId == friend.id,
                                isUpdating = friend.id in uiState.updatingFriendIds,
                                onClick = {
                                    expandedFriendId = if (expandedFriendId == friend.id) null else friend.id
                                },
                                onSendAlarm = { viewModel.openSendAlarm(friend) },
                                onRequestAlarmPermission = {
                                    viewModel.requestAlarmPermission(friend.otherUserId(uiState.currentUserId))
                                },
                                onDelete = {
                                    confirmTarget = FriendConfirmTarget(friend, FriendConfirmAction.Delete)
                                },
                                onBlock = {
                                    confirmTarget = FriendConfirmTarget(friend, FriendConfirmAction.Block)
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showSearchSheet = true },
            containerColor = WakepointPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = WakepointSpacing.screenHorizontal, bottom = BOTTOM_TAB_CLEARANCE + WakepointSpacing.xl)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = WakepointSpacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
            ) {
                Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null)
                Text(text = stringResource(R.string.friends_add), style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas
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
                uiState = uiState,
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
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas
        ) {
            // v2 통합: 친구 알람 보내기는 주소/장소/시간 3탭 형태. 주소 탭만 실기능(백엔드 존재)이고
            // 장소·시간 탭은 도메인/영속성 미구현이라 "준비 중"으로 안내한다.
            var sendTab by remember { mutableStateOf(SendAlarmTab.Address) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WakepointSpacing.screenHorizontal)
                    .padding(top = WakepointSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
            ) {
                SendAlarmSegmentedTabs(selected = sendTab, onSelect = { sendTab = it })
                SendAlarmPrivacyNote()
            }
            when (sendTab) {
                SendAlarmTab.Address -> AlarmSetupSheet(
                    title = friend?.let { "${it.nickname}님에게 알람 보내기" }
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
                SendAlarmTab.Place, SendAlarmTab.Time -> SendAlarmComingSoon(
                    tab = sendTab,
                    onDismiss = { viewModel.dismissSendAlarm() }
                )
            }
        }
    }

    confirmTarget?.let { target ->
        WakepointConfirmDialog(
            title = when (target.action) {
                FriendConfirmAction.Delete -> "친구를 삭제하시겠습니까?"
                FriendConfirmAction.Block -> "친구를 차단하시겠습니까?"
            },
            message = when (target.action) {
                FriendConfirmAction.Delete -> "${target.friend.nickname}님과의 친구 관계가 삭제됩니다."
                FriendConfirmAction.Block -> "${target.friend.nickname}님은 더 이상 친구 목록에 일반 친구로 표시되지 않습니다."
            },
            cancelText = stringResource(R.string.alarm_cancel),
            confirmText = when (target.action) {
                FriendConfirmAction.Delete -> stringResource(R.string.alarm_delete)
                FriendConfirmAction.Block -> "차단"
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

@Composable
private fun EmptyFriendsCard(onAddFriend: () -> Unit) {
    WakepointCard(contentPadding = 24) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            Text(
                text = "아직 친구가 없어요",
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            Text(
                text = "이메일이나 닉네임으로 친구를 검색해\n요청을 보내보세요",
                style = MaterialTheme.typography.bodySmall,
                color = WakepointTextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            WakepointButton(
                text = "친구 검색",
                icon = Icons.Rounded.Search,
                onClick = onAddFriend,
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

@Composable
private fun FriendRequestCard(
    friend: Friend,
    isIncoming: Boolean,
    isUpdating: Boolean,
    onCancel: () -> Unit,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
            ) {
                FriendAvatar(profile = friend.toProfile())
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = WakepointInk
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointTextTertiary
                    )
                }
                FriendStatusPill(status = FriendStatus.Pending)
            }
            if (isIncoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointButton(
                        text = "수락",
                        enabled = !isUpdating,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = "거절",
                        enabled = !isUpdating,
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                WakepointSecondaryButton(
                    text = "요청 취소",
                    enabled = !isUpdating,
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

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
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
            ) {
                if (profile != null) {
                    FriendAvatar(profile = profile)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.nickname ?: "알 수 없는 사용자",
                        style = MaterialTheme.typography.titleMedium,
                        color = WakepointInk
                    )
                    Text(
                        text = if (isIncoming) {
                            "내 알람을 대신 설정할 권한 요청"
                        } else {
                            "상대방 알람 설정 권한 요청 중"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointTextTertiary
                    )
                    Text(
                        text = "만료 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointTextTertiary
                    )
                }
                PermissionStatusPill(status = request.status)
            }
            if (isIncoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointButton(
                        text = "수락",
                        enabled = !isUpdating,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = "거절",
                        enabled = !isUpdating,
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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
    WakepointCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
            ) {
                FriendAvatar(profile = friend.toProfile())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
                ) {
                    Text(
                        text = friend.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = WakepointInk
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = WakepointTextTertiary
                    )
                    PermissionStatusLine(status = friend.permissionStatus)
                }
                FriendStatusPill(
                    status = friend.status,
                    currentUserId = currentUserId,
                    userId = friend.userId
                )
            }

            if (expanded) {
                HorizontalDivider(color = WakepointDivider)
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
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = "차단",
                        icon = Icons.Rounded.Block,
                        enabled = !isUpdating && friend.status != FriendStatus.Blocked,
                        onClick = onBlock,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.alarm_delete),
                        icon = Icons.Rounded.Delete,
                        enabled = !isUpdating,
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    )
                }
                WakepointSecondaryButton(
                    text = when (friend.permissionStatus) {
                        PermissionStatus.Accepted -> "권한 있음"
                        PermissionStatus.Pending -> "권한 대기"
                        else -> "권한 요청"
                    },
                    icon = Icons.Rounded.Lock,
                    enabled = canRequestPermission && !isUpdating,
                    onClick = onRequestAlarmPermission,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** v2 통합: 친구 알람 보내기 방식 3탭. 주소만 실기능, 장소/시간은 백엔드 준비 후. */
private enum class SendAlarmTab(val label: String) {
    Address("주소"),
    Place("장소"),
    Time("시간")
}

@Composable
private fun SendAlarmSegmentedTabs(
    selected: SendAlarmTab,
    onSelect: (SendAlarmTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(WakepointField)
            .padding(WakepointSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        SendAlarmTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(WakepointSize.chipHeight)
                    .clip(WakepointShape.control)
                    .background(if (isSelected) WakepointCanvas else Color.Transparent)
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) WakepointPrimary else WakepointMuted
                )
            }
        }
    }
}

/** v2 정책 안내: 알람을 보내도 상대의 위치/도착 여부는 볼 수 없음. */
@Composable
private fun SendAlarmPrivacyNote() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = WakepointTextTertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "위치나 도착 여부는 볼 수 없어요.",
            style = MaterialTheme.typography.bodySmall,
            color = WakepointTextTertiary
        )
    }
}

@Composable
private fun SendAlarmComingSoon(
    tab: SendAlarmTab,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(top = WakepointSpacing.lg, bottom = WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
    ) {
        BottomSheetTitle(title = "${tab.label} 알람")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WakepointShape.control),
            shape = WakepointShape.control,
            color = WakepointWarningLight
        ) {
            Row(
                modifier = Modifier.padding(WakepointSpacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = WakepointWarning,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${tab.label} 기반 알람은 준비 중이에요.\n지금은 주소 알람만 보낼 수 있어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WakepointInk
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

@Composable
private fun FriendSearchSheet(
    uiState: FriendsUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectResult: (String) -> Unit,
    onSendRequest: (String) -> Unit,
    onShareKakao: () -> Unit,
    onCopyLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        BottomSheetTitle(title = "친구 검색")
        Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                placeholder = "이메일 또는 닉네임",
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = "검색",
                enabled = !uiState.isSearching,
                onClick = onSearch,
                modifier = Modifier.width(86.dp).height(WakepointSize.fieldHeight)
            )
        }

        InviteSection(onShareKakao = onShareKakao, onCopyLink = onCopyLink)

        uiState.searchMessage?.let { message ->
            Text(
                text = message,
                color = WakepointMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            uiState.searchResults.forEach { result ->
                FriendSearchResultRow(
                    result = result,
                    selected = uiState.selectedSearchUserId == result.profile.id,
                    requesting = result.profile.id in uiState.requestingUserIds,
                    onClick = { onSelectResult(result.profile.id) },
                    onSendRequest = { onSendRequest(result.profile.id) }
                )
            }
        }
    }
}

@Composable
private fun InviteSection(
    onShareKakao: () -> Unit,
    onCopyLink: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control),
        shape = WakepointShape.control,
        color = WakepointParchment
    ) {
        Column(
            modifier = Modifier.padding(WakepointSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
        ) {
            Text(
                text = stringResource(R.string.invite_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            Text(
                text = stringResource(R.string.invite_section_hint),
                style = MaterialTheme.typography.bodySmall,
                color = WakepointTextTertiary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                WakepointButton(
                    text = stringResource(R.string.invite_share_kakao),
                    icon = Icons.Rounded.Share,
                    onClick = onShareKakao,
                    modifier = Modifier.weight(1f)
                )
                WakepointSecondaryButton(
                    text = stringResource(R.string.invite_copy_link),
                    icon = Icons.Rounded.Link,
                    onClick = onCopyLink,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
private fun FriendSearchResultRow(
    result: FriendSearchResult,
    selected: Boolean,
    requesting: Boolean,
    onClick: () -> Unit,
    onSendRequest: () -> Unit
) {
    val status = result.existingFriendStatus
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, WakepointPrimary, WakepointShape.control)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = status == null, onClick = onClick),
        shape = WakepointShape.control,
        color = if (selected) WakepointPrimaryLight else WakepointCanvas,
        border = BorderStroke(1.dp, WakepointDivider)
    ) {
        Row(
            modifier = Modifier.padding(WakepointSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
        ) {
            FriendAvatar(profile = result.profile)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
            ) {
                Text(
                    text = result.profile.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = WakepointInk
                )
                Text(
                    text = result.profile.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextTertiary
                )
            }
            if (status != null) {
                FriendStatusPill(status = status)
            } else {
                WakepointButton(
                    text = if (requesting) "요청 중" else "요청",
                    enabled = selected && !requesting,
                    onClick = onSendRequest,
                    modifier = Modifier.width(82.dp).height(WakepointSize.chipHeight)
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusLine(status: PermissionStatus) {
    Text(
        text = when (status) {
            PermissionStatus.None -> "알람 권한 없음"
            PermissionStatus.Pending -> "알람 권한 요청 중"
            PermissionStatus.Accepted -> "알람 권한 있음"
            PermissionStatus.Rejected -> "알람 권한 거절됨"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = WakepointMuted
    )
}

@Composable
private fun PermissionStatusPill(status: PermissionStatus) {
    val text = when (status) {
        PermissionStatus.None -> "권한 없음"
        PermissionStatus.Pending -> "대기"
        PermissionStatus.Accepted -> "승인"
        PermissionStatus.Rejected -> "거절"
    }
    when (status) {
        PermissionStatus.None ->
            StatusPill(text = text, color = WakepointTextTertiary, background = WakepointField)
        PermissionStatus.Pending ->
            StatusPill(text = text, color = WakepointWarning, background = WakepointWarningLight)
        PermissionStatus.Accepted ->
            StatusPill(text = text, color = WakepointPrimary, background = WakepointPrimaryLight)
        PermissionStatus.Rejected ->
            StatusPill(text = text, color = WakepointTextTertiary, background = WakepointField)
    }
}

@Composable
private fun FriendStatusPill(
    status: FriendStatus,
    currentUserId: String = "",
    userId: String = ""
) {
    val isOutgoingPending = status == FriendStatus.Pending && currentUserId.isNotBlank() && currentUserId == userId
    val text = when (status) {
        FriendStatus.Accepted -> "친구"
        FriendStatus.Pending -> if (isOutgoingPending) "요청 중" else "요청 받음"
        FriendStatus.Rejected -> "거절됨"
        FriendStatus.Blocked -> "차단됨"
    }
    when (status) {
        FriendStatus.Accepted ->
            StatusPill(text = text, color = WakepointPrimary, background = WakepointPrimaryLight)
        FriendStatus.Pending ->
            StatusPill(text = text, color = WakepointWarning, background = WakepointWarningLight)
        FriendStatus.Rejected ->
            StatusPill(text = text, color = WakepointTextTertiary, background = WakepointField)
        FriendStatus.Blocked ->
            StatusPill(text = text, color = WakepointDanger, background = WakepointDangerLight)
    }
}

@Composable
private fun FriendAvatar(profile: UserProfile) {
    WakepointAvatar(name = profile.nickname)
}

private fun Friend.toProfile(): UserProfile = UserProfile(
    id = friendId,
    email = email,
    nickname = nickname,
    avatarUrl = avatarUrl,
    pushToken = null
)

private fun Friend.otherUserId(currentUserId: String): String {
    return if (friendId == currentUserId) userId else friendId
}
