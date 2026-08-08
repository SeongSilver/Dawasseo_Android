package com.wakepoint.app.feature.v2.friendalarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointPositive
import com.wakepoint.app.core.design.WakepointPositiveLight
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.design.WakepointTintButton
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight
import com.wakepoint.app.feature.v2.model.FriendPermission

/** 친구 목록 UI 모델(표현 전용). */
data class V2Friend(val name: String, val email: String, val permission: FriendPermission)

/**
 * v2 STEP3 (3a) — 친구 목록.
 * 받은 친구 요청(수락/거절) · 받은 권한 요청(허용/거절) · 내 친구 목록(권한 3상태 배지 + 확장 액션 4종).
 */
@Composable
fun FriendListScreen(
    friends: List<V2Friend>,
    modifier: Modifier = Modifier,
    incomingRequests: List<V2Friend> = emptyList(),
    permissionRequests: List<V2Friend> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointBackground),
    ) {
        WakepointPageHeader(title = "친구")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.md),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionGap),
        ) {
            if (incomingRequests.isNotEmpty()) {
                Section("받은 친구 요청") {
                    incomingRequests.forEach { RequestCard(it, "수락", "거절") }
                }
            }
            if (permissionRequests.isNotEmpty()) {
                Section("받은 권한 요청") {
                    permissionRequests.forEach { RequestCard(it, "허용", "거절") }
                }
            }
            Section("내 친구") {
                friends.forEach { FriendCard(it) }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sectionTitleGap)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)) { content() }
    }
}

@Composable
private fun RequestCard(friend: V2Friend, positiveText: String, negativeText: String) {
    WakepointCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            WakepointAvatar(name = friend.name)
            Column(Modifier.weight(1f)) {
                Text(friend.name, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
                Text(friend.email, style = MaterialTheme.typography.bodySmall, color = WakepointTextTertiary)
            }
        }
        Spacer(Modifier.height(WakepointSpacing.md))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointSecondaryButton(negativeText, Modifier.weight(1f))
            WakepointTintButton(positiveText, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FriendCard(friend: V2Friend) {
    var expanded by remember { mutableStateOf(false) }
    WakepointCard(modifier = Modifier.clickable { expanded = !expanded }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            WakepointAvatar(name = friend.name)
            Column(Modifier.weight(1f)) {
                Text(friend.name, style = MaterialTheme.typography.titleMedium, color = WakepointInk)
                Text(friend.email, style = MaterialTheme.typography.bodySmall, color = WakepointTextTertiary)
            }
            PermissionBadge(friend.permission)
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(WakepointSpacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointTintButton("알람 보내기", Modifier.weight(1f))
                    WakepointSecondaryButton("권한 요청", Modifier.weight(1f))
                }
                Spacer(Modifier.height(WakepointSpacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
                    WakepointSecondaryButton("차단", Modifier.weight(1f))
                    com.wakepoint.app.core.design.WakepointDangerButton("삭제", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PermissionBadge(permission: FriendPermission) {
    when (permission) {
        FriendPermission.ALLOWED -> StatusPill(permission.label, color = WakepointPositive, background = WakepointPositiveLight)
        FriendPermission.PENDING -> StatusPill(permission.label, color = WakepointWarning, background = WakepointWarningLight)
        FriendPermission.DENIED -> StatusPill(permission.label, color = WakepointTextSecondary)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun FriendListScreenPreview() {
    WakepointTheme {
        FriendListScreen(
            incomingRequests = listOf(V2Friend("이서연", "seoyeon@example.com", FriendPermission.DENIED)),
            permissionRequests = listOf(V2Friend("박지수", "jisu@example.com", FriendPermission.PENDING)),
            friends = listOf(
                V2Friend("김민수", "minsu@example.com", FriendPermission.ALLOWED),
                V2Friend("최지훈", "jihoon@example.com", FriendPermission.PENDING),
                V2Friend("정하나", "hana@example.com", FriendPermission.DENIED),
            ),
        )
    }
}
