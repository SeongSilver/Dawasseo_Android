package com.wakepoint.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.data.mock.MockWakepointData
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.feature.home.AlarmSetupSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen() {
    var expandedFriendId by remember { mutableStateOf<String?>(null) }
    var showSendAlarm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
    ) {
        Column {
            WakepointHeader(
                action = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Rounded.GroupAdd, contentDescription = null)
                    }
                }
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                WakepointTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = stringResource(R.string.friends_search),
                    leadingIcon = Icons.Rounded.Search,
                    readOnly = true
                )
                Text(
                    text = stringResource(R.string.friends_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = WakepointInk
                )
                MockWakepointData.friends.forEachIndexed { index, friend ->
                    FriendRow(
                        friend = friend,
                        starred = index == 0,
                        expanded = expandedFriendId == friend.id,
                        onClick = {
                            expandedFriendId = if (expandedFriendId == friend.id) null else friend.id
                        },
                        onSendAlarm = { showSendAlarm = true }
                    )
                }
                FriendRow(
                    friend = Friend(
                        id = "friend-row-3",
                        userId = "user-1",
                        friendId = "friend-3",
                        nickname = "박수진",
                        email = "sujin@example.com",
                        permissionStatus = MockWakepointData.friends.first().permissionStatus
                    ),
                    starred = false,
                    expanded = false,
                    onClick = {},
                    onSendAlarm = {}
                )
            }
        }

        FloatingActionButton(
            onClick = {},
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
                Text(text = "+", style = MaterialTheme.typography.titleLarge)
                Text(text = stringResource(R.string.friends_add), style = MaterialTheme.typography.labelLarge)
            }
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
                onDismiss = { showSendAlarm = false }
            )
        }
    }
}

@Composable
private fun FriendRow(
    friend: Friend,
    starred: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    onSendAlarm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FriendAvatar(name = friend.nickname)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WakepointInk
                )
                if (expanded) {
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WakepointMuted
                    )
                }
            }
            Icon(
                imageVector = if (starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (starred) WakepointPrimary else WakepointMuted
            )
        }
        if (expanded) {
            WakepointButton(
                text = stringResource(R.string.friends_send_alarm),
                onClick = onSendAlarm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, bottom = 12.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFDADDE2))
    }
}

@Composable
private fun FriendAvatar(name: String) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = if (name.length > 2) Color(0xFFE9EAEC) else Color(0xFFDDE7F8)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1),
                color = WakepointInk,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
