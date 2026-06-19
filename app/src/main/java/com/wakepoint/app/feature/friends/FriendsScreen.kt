package com.wakepoint.app.feature.friends

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.UserProfile
import com.wakepoint.app.feature.home.AlarmSetupSheet

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
    var showSendAlarm by remember { mutableStateOf(false) }
    var confirmTarget by remember { mutableStateOf<FriendConfirmTarget?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        Column {
            WakepointHeader(
                action = {
                    Row {
                        IconButton(onClick = viewModel::refreshFriends) {
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
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    Text(
                        text = message,
                        color = WakepointMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = stringResource(R.string.friends_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = WakepointInk
                )

                if (uiState.friends.isEmpty()) {
                    EmptyFriendsCard(onAddFriend = { showSearchSheet = true })
                } else {
                    uiState.friends.forEach { friend ->
                        key(friend.id) {
                            FriendCard(
                                friend = friend,
                                currentUserId = uiState.currentUserId,
                                expanded = expandedFriendId == friend.id,
                                isUpdating = friend.id in uiState.updatingFriendIds,
                                onClick = {
                                    expandedFriendId = if (expandedFriendId == friend.id) null else friend.id
                                },
                                onSendAlarm = { showSendAlarm = true },
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
                .padding(end = 20.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = 10.dp)) },
            containerColor = WakepointCanvas
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

    if (showSendAlarm) {
        ModalBottomSheet(
            onDismissRequest = { showSendAlarm = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = 10.dp)) },
            containerColor = WakepointCanvas
        ) {
            AlarmSetupSheet(
                title = stringResource(R.string.alarm_send_title),
                primaryButton = stringResource(R.string.alarm_send),
                showAddressSearch = true,
                onPrimaryClick = { showSendAlarm = false },
                onDismiss = { showSendAlarm = false }
            )
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
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "아직 친구가 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = WakepointInk
            )
            Text(
                text = "이메일이나 닉네임으로 친구를 검색해 요청을 보내보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted
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
private fun FriendCard(
    friend: Friend,
    currentUserId: String,
    expanded: Boolean,
    isUpdating: Boolean,
    onClick: () -> Unit,
    onSendAlarm: () -> Unit,
    onDelete: () -> Unit,
    onBlock: () -> Unit
) {
    WakepointCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FriendAvatar(profile = friend.toProfile())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = friend.nickname,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WakepointInk,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WakepointMuted
                    )
                }
                FriendStatusPill(
                    status = friend.status,
                    currentUserId = currentUserId,
                    userId = friend.userId
                )
            }

            if (expanded) {
                HorizontalDivider(color = WakepointParchment)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WakepointButton(
                        text = stringResource(R.string.friends_send_alarm),
                        icon = Icons.AutoMirrored.Rounded.Send,
                        enabled = friend.status == FriendStatus.Accepted && !isUpdating,
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
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "친구 검색",
            style = MaterialTheme.typography.titleLarge,
            color = WakepointInk,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                modifier = Modifier.size(width = 86.dp, height = 56.dp)
            )
        }

        uiState.searchMessage?.let { message ->
            Text(
                text = message,
                color = WakepointMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier.border(1.dp, WakepointPrimary, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = status == null, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) WakepointPrimary.copy(alpha = 0.06f) else WakepointParchment,
        border = BorderStroke(1.dp, Color(0xFFE3E5E8))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FriendAvatar(profile = result.profile)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = result.profile.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WakepointInk
                )
                Text(
                    text = result.profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WakepointMuted
                )
            }
            if (status != null) {
                FriendStatusPill(status = status)
            } else {
                WakepointButton(
                    text = if (requesting) "요청 중" else "요청",
                    enabled = selected && !requesting,
                    onClick = onSendRequest,
                    modifier = Modifier.size(width = 82.dp, height = 44.dp)
                )
            }
        }
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
    val color = when (status) {
        FriendStatus.Accepted -> WakepointPrimary
        FriendStatus.Pending -> Color(0xFF8A6D1D)
        FriendStatus.Rejected -> WakepointMuted
        FriendStatus.Blocked -> Color(0xFFB3261E)
    }
    StatusPill(text = text, color = color)
}

@Composable
private fun FriendAvatar(profile: UserProfile) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = if (profile.avatarUrl.isNullOrBlank()) Color(0xFFE9EAEC) else Color(0xFFDDE7F8)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = profile.nickname.take(1).ifBlank { "?" },
                color = WakepointInk,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun Friend.toProfile(): UserProfile = UserProfile(
    id = friendId,
    email = email,
    nickname = nickname,
    avatarUrl = avatarUrl,
    pushToken = null
)
