package com.wakepoint.app.feature.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.ButtonRow
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.SectionLabel
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.domain.model.AlarmInvitation
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.InvitationStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.UserProfile
import kotlinx.coroutines.delay

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
    val uiState by viewModel.uiState.collectAsState()
    var expandedFriendId by remember { mutableStateOf<String?>(null) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var confirmTarget by remember { mutableStateOf<FriendConfirmTarget?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val defaultAlarmLabel = stringResource(R.string.alarm_default_label)

    // 화면이 보이는 동안 친구, 권한, 초대 상태를 주기적으로 다시 불러온다.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                viewModel.refreshAll()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            WakepointHeader(
                action = {
                    Row {
                        IconButton(onClick = viewModel::refreshAll) {
                            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                        }
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(imageVector = Icons.Rounded.GroupAdd, contentDescription = null)
                        }
                    }
                }
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WakepointTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = stringResource(R.string.friends_search),
                    leadingIcon = Icons.Rounded.Search,
                    readOnly = true,
                    modifier = Modifier.clickable { showSearchSheet = true }
                )

                uiState.errorMessage?.let { message ->
                    MessageText(text = message.resolve())
                }

                val hasFriendContent = uiState.acceptedFriends.isNotEmpty() ||
                    uiState.receivedFriendRequests.isNotEmpty() ||
                    uiState.sentFriendRequests.isNotEmpty() ||
                    uiState.receivedAlarmPermissionRequests.isNotEmpty() ||
                    uiState.sentAlarmPermissionRequests.isNotEmpty() ||
                    uiState.receivedInvitations.isNotEmpty() ||
                    uiState.sentInvitations.isNotEmpty()

                if (!hasFriendContent) {
                    EmptyFriendsCard(onAddFriend = { showSearchSheet = true })
                } else {
                    if (uiState.receivedInvitations.isNotEmpty()) {
                        SectionLabel(text = stringResource(R.string.friends_section_received_invitations))
                        uiState.receivedInvitations.forEach { invitation ->
                            key("invitation-received-${invitation.id}") {
                                AlarmInvitationCard(
                                    invitation = invitation,
                                    isIncoming = true,
                                    isUpdating = invitation.id in uiState.updatingInvitationIds,
                                    onAccept = { viewModel.acceptAlarmInvitation(invitation) },
                                    onReject = { viewModel.rejectAlarmInvitation(invitation.id) },
                                    onCancel = {}
                                )
                            }
                        }
                    }

                    if (uiState.sentInvitations.isNotEmpty()) {
                        SectionLabel(text = stringResource(R.string.friends_section_sent_invitations))
                        uiState.sentInvitations.forEach { invitation ->
                            key("invitation-sent-${invitation.id}") {
                                AlarmInvitationCard(
                                    invitation = invitation,
                                    isIncoming = false,
                                    isUpdating = invitation.id in uiState.updatingInvitationIds,
                                    onAccept = {},
                                    onReject = {},
                                    onCancel = { viewModel.cancelAlarmInvitation(invitation.id) }
                                )
                            }
                        }
                    }

                    if (uiState.receivedFriendRequests.isNotEmpty()) {
                        SectionLabel(text = stringResource(R.string.friends_section_received_requests))
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
                        SectionLabel(text = stringResource(R.string.friends_section_sent_requests))
                        uiState.sentFriendRequests.forEach { friend ->
                            key("sent-${friend.id}") {
                                FriendRequestCard(
                                    friend = friend,
                                    isIncoming = false,
                                    isUpdating = friend.id in uiState.updatingFriendIds,
                                    onAccept = {},
                                    onReject = {},
                                    onCancel = { viewModel.deleteFriend(friend.id) }
                                )
                            }
                        }
                    }

                    if (uiState.receivedAlarmPermissionRequests.isNotEmpty()) {
                        SectionLabel(text = stringResource(R.string.friends_section_received_permissions))
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
                        SectionLabel(text = stringResource(R.string.friends_section_sent_permissions))
                        uiState.sentAlarmPermissionRequests.forEach { request ->
                            key("permission-sent-${request.id}") {
                                AlarmPermissionRequestCard(
                                    request = request,
                                    currentUserId = uiState.currentUserId,
                                    isUpdating = request.id in uiState.updatingPermissionIds,
                                    onAccept = {},
                                    onReject = {}
                                )
                            }
                        }
                    }

                    if (uiState.acceptedFriends.isNotEmpty()) {
                        SectionLabel(text = stringResource(R.string.friends_section))
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

        ExtendedFloatingActionButton(
            onClick = { showSearchSheet = true },
            icon = { Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = null) },
            text = { Text(text = stringResource(R.string.friends_add)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() }
        ) {
            FriendSearchSheet(
                uiState = uiState,
                onQueryChange = viewModel::updateSearchQuery,
                onSearch = viewModel::searchUsers,
                onSelectResult = viewModel::selectSearchResult,
                onSendRequest = viewModel::sendFriendRequest
            )
        }
    }

    if (uiState.sendTarget != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissSendAlarm,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle() }
        ) {
            SendAlarmSheet(
                uiState = uiState,
                onLabelChange = viewModel::updateSendLabel,
                onRadiusChange = viewModel::updateSendRadiusOption,
                onSearchQueryChange = viewModel::updateSendSearchQuery,
                onSearch = viewModel::searchSendDestination,
                onSelectPlace = viewModel::selectSendDestination,
                onSend = { viewModel.sendAlarmInvitation(fallbackLabel = defaultAlarmLabel) },
                onDismiss = viewModel::dismissSendAlarm
            )
        }
    }

    confirmTarget?.let { target ->
        WakepointConfirmDialog(
            title = when (target.action) {
                FriendConfirmAction.Delete -> stringResource(R.string.friends_delete_confirm_title)
                FriendConfirmAction.Block -> stringResource(R.string.friends_block_confirm_title)
            },
            message = when (target.action) {
                FriendConfirmAction.Delete ->
                    stringResource(R.string.friends_delete_confirm_message, target.friend.nickname)
                FriendConfirmAction.Block ->
                    stringResource(R.string.friends_block_confirm_message, target.friend.nickname)
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

@Composable
private fun EmptyFriendsCard(onAddFriend: () -> Unit) {
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.friends_empty_title),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.friends_empty_body),
                style = MaterialTheme.typography.bodyMedium
            )
            WakepointButton(
                text = stringResource(R.string.friends_search_action),
                icon = Icons.Rounded.Search,
                onClick = onAddFriend,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 알람 초대 카드. 요청자(보낸 쪽)에게는 초대 처리 상태만 보여준다.
 * 상대의 위치, 도착, 알람 울림 여부는 어떤 형태로도 표시하지 않는다.
 */
@Composable
private fun AlarmInvitationCard(
    invitation: AlarmInvitation,
    isIncoming: Boolean,
    isUpdating: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    val counterpart = if (isIncoming) invitation.requesterProfile else invitation.targetProfile
    val counterpartName = counterpart?.nickname ?: stringResource(R.string.friends_unknown_user)
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (counterpart != null) {
                    FriendAvatar(profile = counterpart)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isIncoming) {
                            stringResource(R.string.invite_incoming_from, counterpartName)
                        } else {
                            stringResource(R.string.invite_outgoing_to, counterpartName)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.alarm_list_location_prefix, invitation.label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (invitation.targetAddress.isNotBlank()) {
                        Text(
                            text = invitation.targetAddress,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = stringResource(R.string.alarm_radius_line, radiusKmToOption(invitation.radiusKm)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                InvitationStatusPill(status = invitation.status)
            }
            if (isIncoming) {
                ButtonRow {
                    WakepointButton(
                        text = stringResource(R.string.friends_accept),
                        enabled = !isUpdating,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_reject),
                        enabled = !isUpdating,
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else if (invitation.status == InvitationStatus.Pending) {
                WakepointSecondaryButton(
                    text = stringResource(R.string.invite_cancel),
                    enabled = !isUpdating,
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FriendRequestCard(
    friend: Friend,
    isIncoming: Boolean,
    isUpdating: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FriendAvatar(profile = friend.toProfile())
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.nickname,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                FriendStatusPill(status = FriendStatus.Pending)
            }
            if (isIncoming) {
                ButtonRow {
                    WakepointButton(
                        text = stringResource(R.string.friends_accept),
                        enabled = !isUpdating,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_reject),
                        enabled = !isUpdating,
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                WakepointSecondaryButton(
                    text = stringResource(R.string.friends_cancel_request),
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
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val isIncoming = request.targetId == currentUserId
    val profile = if (isIncoming) request.requesterProfile else request.targetProfile
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (profile != null) {
                    FriendAvatar(profile = profile)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.nickname ?: stringResource(R.string.friends_unknown_user),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (isIncoming) {
                            stringResource(R.string.friends_permission_incoming_desc)
                        } else {
                            stringResource(R.string.friends_permission_outgoing_desc)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.friends_no_expiry),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                PermissionStatusPill(status = request.status)
            }
            if (isIncoming) {
                ButtonRow {
                    WakepointButton(
                        text = stringResource(R.string.friends_accept),
                        enabled = !isUpdating,
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_reject),
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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FriendAvatar(profile = friend.toProfile())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = friend.nickname,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium
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
                HorizontalDivider()
                val canSendAlarm = friend.status == FriendStatus.Accepted &&
                    friend.permissionStatus == PermissionStatus.Accepted
                val canRequestPermission = friend.status == FriendStatus.Accepted &&
                    friend.permissionStatus == PermissionStatus.None
                ButtonRow {
                    WakepointButton(
                        text = stringResource(R.string.friends_send_alarm),
                        icon = Icons.AutoMirrored.Rounded.Send,
                        enabled = canSendAlarm && !isUpdating,
                        onClick = onSendAlarm,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointSecondaryButton(
                        text = stringResource(R.string.friends_block),
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
                        PermissionStatus.Accepted -> stringResource(R.string.friends_permission_granted)
                        PermissionStatus.Pending -> stringResource(R.string.friends_permission_waiting)
                        else -> stringResource(R.string.friends_permission_request)
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

@Composable
private fun FriendSearchSheet(
    uiState: FriendsUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectResult: (String) -> Unit,
    onSendRequest: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.friends_search),
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WakepointTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.friends_search_placeholder),
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.weight(1f)
            )
            WakepointButton(
                text = stringResource(R.string.friends_search_button),
                enabled = !uiState.isSearching,
                onClick = onSearch
            )
        }

        uiState.searchMessage?.let { message ->
            MessageText(text = message.resolve())
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun FriendSearchResultRow(
    result: FriendSearchResult,
    selected: Boolean,
    requesting: Boolean,
    onClick: () -> Unit,
    onSendRequest: () -> Unit
) {
    val status = result.existingFriendStatus
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = status == null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FriendAvatar(profile = result.profile)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = result.profile.nickname,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = result.profile.email,
                    style = MaterialTheme.typography.bodyMedium
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
                    onClick = onSendRequest
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusLine(status: PermissionStatus) {
    Text(
        text = when (status) {
            PermissionStatus.None -> stringResource(R.string.friends_permission_line_none)
            PermissionStatus.Pending -> stringResource(R.string.friends_permission_line_pending)
            PermissionStatus.Accepted -> stringResource(R.string.friends_permission_line_accepted)
            PermissionStatus.Rejected -> stringResource(R.string.friends_permission_line_rejected)
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun PermissionStatusPill(status: PermissionStatus) {
    val text = when (status) {
        PermissionStatus.None -> stringResource(R.string.friends_permission_pill_none)
        PermissionStatus.Pending -> stringResource(R.string.friends_permission_pill_pending)
        PermissionStatus.Accepted -> stringResource(R.string.friends_permission_pill_accepted)
        PermissionStatus.Rejected -> stringResource(R.string.friends_permission_pill_rejected)
    }
    StatusPill(text = text, color = Color.Unspecified)
}

@Composable
private fun InvitationStatusPill(status: InvitationStatus) {
    val text = when (status) {
        InvitationStatus.Pending -> stringResource(R.string.invite_status_pending)
        InvitationStatus.Accepted -> stringResource(R.string.invite_status_accepted)
        InvitationStatus.Rejected -> stringResource(R.string.invite_status_rejected)
        InvitationStatus.Expired -> stringResource(R.string.invite_status_expired)
        InvitationStatus.Cancelled -> stringResource(R.string.invite_status_cancelled)
    }
    StatusPill(text = text, color = Color.Unspecified)
}

@Composable
private fun FriendStatusPill(
    status: FriendStatus,
    currentUserId: String = "",
    userId: String = ""
) {
    val isOutgoingPending = status == FriendStatus.Pending && currentUserId.isNotBlank() && currentUserId == userId
    val text = when (status) {
        FriendStatus.Accepted -> stringResource(R.string.friends_status_friend)
        FriendStatus.Pending -> if (isOutgoingPending) {
            stringResource(R.string.friends_status_outgoing)
        } else {
            stringResource(R.string.friends_status_incoming)
        }
        FriendStatus.Rejected -> stringResource(R.string.friends_status_rejected)
        FriendStatus.Blocked -> stringResource(R.string.friends_status_blocked)
    }
    StatusPill(text = text, color = Color.Unspecified)
}

@Composable
private fun FriendAvatar(profile: UserProfile) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = profile.nickname.take(1).ifBlank { "?" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun Friend.toProfile(): UserProfile = UserProfile(
    id = friendId,
    email = email,
    nickname = nickname,
    avatarUrl = avatarUrl,
    pushToken = null
)

private const val AUTO_REFRESH_INTERVAL_MS = 30_000L
