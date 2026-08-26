package com.wakepoint.app.e2e.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import com.wakepoint.app.feature.friends.ReceiveAlarmDialog
import com.wakepoint.app.feature.friends.SentAlarmItem
import com.wakepoint.app.feature.friends.SentAlarmsContent
import com.wakepoint.app.feature.friends.SentAlarmsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E(계측): v2 STEP3 친구 알람 — 보낸 알람 목록(3-6)·수신 다이얼로그(3-7)를
 * 실제 렌더링·상호작용으로 검증한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FriendAlarmE2ETest {

    @get:Rule
    val compose = createComposeRule()

    private fun alarm(id: String = "a1", label: String = "강남역 도착") = Alarm(
        id = id, ownerId = "friend-1", createdBy = "me", label = label,
        targetLat = 37.0, targetLng = 127.0, targetAddress = "서울 강남구 강남대로",
        radiusKm = 0.3, isActive = true, soundType = SoundType.Default, soundUri = null,
        soundEnabled = true, vibrate = true,
    )

    // --- 3-6 보낸 알람 목록 ---

    @Test
    fun sentAlarms_showsStatusPills_andCancelConfirm() {
        var canceledId: String? = null
        compose.setContent {
            WakepointTheme {
                SentAlarmsContent(
                    uiState = SentAlarmsUiState(
                        isLoading = false,
                        items = listOf(
                            SentAlarmItem(alarm("p1", "회사 앞"), "민수", pending = true),
                            SentAlarmItem(alarm("s1"), "지수", pending = false),
                        ),
                    ),
                    onBack = {},
                    onCancel = { canceledId = it },
                    onDismissError = {},
                )
            }
        }
        compose.onNodeWithText("전송까지만 보여요. 이후는 친구의 알람이에요.").assertIsDisplayed()
        compose.onNodeWithText("민수님 · 회사 앞").assertIsDisplayed()
        compose.onNodeWithText("전송 대기").assertIsDisplayed()
        compose.onNodeWithText("지수님 · 강남역 도착").assertIsDisplayed()
        compose.onNodeWithText("전송됨").assertIsDisplayed()
        // 첫 카드의 [취소] → 확인 다이얼로그 → [알람 취소] → 콜백
        compose.onAllNodesWithText("취소")[0].performClick()
        compose.onNodeWithText("보낸 알람을 취소할까요?").assertIsDisplayed()
        compose.onNodeWithText("알람 취소").performClick()
        assertEquals("p1", canceledId)
    }

    @Test
    fun sentAlarms_emptyState() {
        compose.setContent {
            WakepointTheme {
                SentAlarmsContent(
                    uiState = SentAlarmsUiState(isLoading = false, items = emptyList()),
                    onBack = {},
                    onCancel = {},
                    onDismissError = {},
                )
            }
        }
        compose.onNodeWithText("아직 보낸 알람이 없어요.").assertIsDisplayed()
    }

    // --- 3-7 수신 다이얼로그 ---

    @Test
    fun receiveDialog_showsSpec_andActivateCallback() {
        var activated = false
        compose.setContent {
            WakepointTheme {
                ReceiveAlarmDialog(
                    alarm = alarm(),
                    senderName = "지수",
                    onActivate = { activated = true },
                    onReject = {},
                )
            }
        }
        compose.onNodeWithText("지수님이 알람을 보냈어요").assertIsDisplayed()
        compose.onNodeWithText("위치 알람").assertIsDisplayed()
        compose.onNodeWithText("강남역 도착").assertIsDisplayed()
        compose.onNodeWithText("서울 강남구 강남대로").assertIsDisplayed()
        compose.onNodeWithText("300m").assertIsDisplayed()
        compose.onNodeWithText("활성화").performClick()
        assertTrue(activated)
    }

    @Test
    fun receiveDialog_rejectCallback() {
        var rejected = false
        compose.setContent {
            WakepointTheme {
                ReceiveAlarmDialog(
                    alarm = alarm(),
                    senderName = "지수",
                    onActivate = {},
                    onReject = { rejected = true },
                )
            }
        }
        compose.onNodeWithText("거절").performClick()
        assertTrue(rejected)
    }
}
