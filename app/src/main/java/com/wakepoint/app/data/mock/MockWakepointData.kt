package com.wakepoint.app.data.mock

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.SoundType
import com.wakepoint.app.domain.model.UserProfile

object MockWakepointData {
    val currentUser = UserProfile(
        id = "user-1",
        email = "me@example.com",
        nickname = "나",
        avatarUrl = null,
        pushToken = null
    )

    val alarms = listOf(
        Alarm(
            id = "alarm-1",
            ownerId = "user-1",
            createdBy = "user-1",
            label = "집 근처 도착",
            targetLat = 37.5665,
            targetLng = 126.9780,
            targetAddress = "서울특별시 중구 세종대로",
            radiusKm = 0.5,
            isActive = true,
            triggeredAt = null,
            soundType = SoundType.Default,
            soundUri = null
        ),
        Alarm(
            id = "alarm-2",
            ownerId = "user-1",
            createdBy = "friend-1",
            label = "친구가 설정한 귀가 알람",
            targetLat = 37.3947,
            targetLng = 127.1112,
            targetAddress = "판교역",
            radiusKm = 1.0,
            isActive = true,
            triggeredAt = null,
            soundType = SoundType.Default,
            soundUri = null
        ),
        Alarm(
            id = "alarm-3",
            ownerId = "user-1",
            createdBy = "user-1",
            label = "지난 목적지",
            targetLat = 37.4979,
            targetLng = 127.0276,
            targetAddress = "강남역",
            radiusKm = 0.3,
            isActive = false,
            triggeredAt = "2026-06-14T22:10:00Z",
            soundType = SoundType.Custom,
            soundUri = "user-1/1781455800000.m4a"
        )
    )

    val friends = listOf(
        Friend(
            id = "friend-row-1",
            userId = "user-1",
            friendId = "friend-1",
            nickname = "민준",
            email = "minjun@example.com",
            permissionStatus = PermissionStatus.Accepted
        ),
        Friend(
            id = "friend-row-2",
            userId = "user-1",
            friendId = "friend-2",
            nickname = "엄마",
            email = "family@example.com",
            permissionStatus = PermissionStatus.Pending
        )
    )
}
