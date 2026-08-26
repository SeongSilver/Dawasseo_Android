package com.wakepoint.app.e2e.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.feature.lasttrain.FriendLastTrainReceive
import com.wakepoint.app.feature.lasttrain.FriendLastTrainSendCard
import com.wakepoint.app.feature.lasttrain.LastTrainCard
import com.wakepoint.app.feature.lasttrain.LastTrainEndedCard
import com.wakepoint.app.feature.lasttrain.LastTrainNoHomeCard
import com.wakepoint.app.feature.lasttrain.LastTrainResult
import com.wakepoint.app.feature.lasttrain.LocationConsentSheetContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E(계측): v2 STEP4 막차 — 3단계 카드(4-1)·끊김(4-2)·집 미등록(4-3)·
 * 위치 동의(4-5)·친구 막차(4-6)를 실제 렌더링·상호작용으로 검증한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LastTrainE2ETest {

    @get:Rule
    val compose = createComposeRule()

    private fun available(remaining: Int, fare: String? = null) = LastTrainResult.Available(
        remainingMinutes = remaining,
        deadlineHour = 23,
        deadlineMinute = 2,
        stationName = "선릉역",
        bottleneck = "도보 8분 + 환승 대기",
        taxiFare = fare,
    )

    @Test
    fun relaxedCard_showsHero_deadline_andAlarmAction() {
        var alarmSet = false
        compose.setContent {
            WakepointTheme {
                LastTrainCard(state = available(23), onSetDeadlineAlarm = { alarmSet = true })
            }
        }
        compose.onNodeWithText("여유").assertIsDisplayed()
        compose.onNodeWithText("23분").assertIsDisplayed()
        compose.onNodeWithText("선릉역 오후 11:02").assertIsDisplayed()
        compose.onNodeWithText("택시비 탭해서 조회").assertIsDisplayed()
        compose.onNodeWithText("선릉역 오후 11:02에 알람 걸기").performClick()
        assertTrue(alarmSet)
    }

    @Test
    fun imminentAndCriticalCards_showLevelActions() {
        compose.setContent {
            WakepointTheme {
                androidx.compose.foundation.layout.Column {
                    LastTrainCard(state = available(9, fare = "약 12,000원"))
                    LastTrainCard(state = available(-4, fare = "약 28,000원"))
                }
            }
        }
        compose.onNodeWithText("임박").assertIsDisplayed()
        compose.onNodeWithText("지금 알람 걸기").assertIsDisplayed()
        compose.onNodeWithText("아슬").assertIsDisplayed()
        compose.onNodeWithText("알람은 이미 늦었어요").assertIsDisplayed()
        compose.onNodeWithText("경로 보기").assertIsDisplayed()
    }

    @Test
    fun endedCard_showsFirstTrainAlarmAction() {
        var firstTrainAlarm = false
        compose.setContent {
            WakepointTheme {
                LastTrainEndedCard(
                    state = LastTrainResult.Ended(5, 34, "약 28,000원", "심야할증 포함 · 약 25분"),
                    onSetFirstTrainAlarm = { firstTrainAlarm = true },
                )
            }
        }
        compose.onNodeWithText("오늘은 대중교통이 끊겼어요").assertIsDisplayed()
        compose.onNodeWithText("첫차 오전 5:34").assertIsDisplayed()
        compose.onNodeWithText("첫차 오전 5:34에 알람 걸기").performClick()
        assertTrue(firstTrainAlarm)
    }

    @Test
    fun noHomeCard_promptsRegistration() {
        var registered = false
        compose.setContent {
            WakepointTheme { LastTrainNoHomeCard(onRegisterHome = { registered = true }) }
        }
        compose.onNodeWithText("집을 등록하면 막차를 챙겨드릴게요").assertIsDisplayed()
        compose.onNodeWithText("주소는 나만 볼 수 있어요.").assertIsDisplayed()
        compose.onNodeWithText("집 등록하기").performClick()
        assertTrue(registered)
    }

    @Test
    fun consentSheet_showsThreeRows_andAgree() {
        var agreed = false
        compose.setContent {
            WakepointTheme { LocationConsentSheetContent(onAgree = { agreed = true }, onLater = {}) }
        }
        compose.onNodeWithText("현재 위치를 카카오맵에 전달할까요?").assertIsDisplayed()
        compose.onNodeWithText("전달 항목").assertIsDisplayed()
        compose.onNodeWithText("전달 시점").assertIsDisplayed()
        compose.onNodeWithText("저장 여부").assertIsDisplayed()
        compose.onNodeWithText("동의하고 조회").performClick()
        assertTrue(agreed)
    }

    @Test
    fun friendSend_showsNotSentList_andReceiveShowsFunTone() {
        compose.setContent {
            WakepointTheme {
                androidx.compose.foundation.layout.Column {
                    FriendLastTrainSendCard(recipientName = "민수")
                    FriendLastTrainReceive(state = available(18, fare = "약 28,000원"))
                }
            }
        }
        compose.onNodeWithText("민수님에게 막차 알람 보내기").assertIsDisplayed()
        compose.onNodeWithText("전달되지 않는 것").assertIsDisplayed()
        compose.onNodeWithText("집 주소").assertIsDisplayed()
        compose.onNodeWithText("막차 시각").assertIsDisplayed()
        compose.onNodeWithText("약 28,000원 ≈ 치킨 1.5마리").assertIsDisplayed()
    }
}
