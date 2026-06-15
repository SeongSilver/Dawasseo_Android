package com.wakepoint.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSuccess
import com.wakepoint.app.data.mock.MockWakepointData
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.PermissionStatus

@Composable
fun FriendsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.friends_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )
        WakepointButton(text = stringResource(R.string.friends_search))
        MockWakepointData.friends.forEach { friend ->
            FriendCard(friend = friend)
        }
    }
}

@Composable
private fun FriendCard(friend: Friend) {
    val label = when (friend.permissionStatus) {
        PermissionStatus.Accepted -> stringResource(R.string.permission_accepted)
        PermissionStatus.Pending -> stringResource(R.string.permission_pending)
        PermissionStatus.Rejected -> stringResource(R.string.permission_rejected)
    }
    val color = when (friend.permissionStatus) {
        PermissionStatus.Accepted -> WakepointSuccess
        PermissionStatus.Pending -> WakepointPrimary
        PermissionStatus.Rejected -> WakepointMuted
    }

    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(text = label, color = color)
            Text(
                text = friend.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            Text(
                text = friend.email,
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted
            )
        }
    }
}
