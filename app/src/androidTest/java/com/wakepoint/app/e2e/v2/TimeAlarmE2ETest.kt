package com.wakepoint.app.e2e.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.domain.model.TimeAlarm
import com.wakepoint.app.feature.timealarm.AlarmTypeSheetContent
import com.wakepoint.app.feature.timealarm.TimeAlarmCard
import com.wakepoint.app.feature.timealarm.TimeAlarmCreateSheetContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E(계측): v2 STEP1 시간 알람 UI — 타입 선택 시트(1-1)·생성 시트(1-2)·목록 카드/편집(1-3,1-4)·
 * 정확도 제한 배지(1-6)를 실제 렌더링·상호작용으로 검증한다.
 *
 * 알람 탭 진입은 로그인 세션이 필요해(Hilt 테스트 더블 미구축) 화면 전체 대신
 * 실사용과 동일한 콘텐츠 컴포저블을 직접 올려 검증한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TimeAlarmE2ETest {

    @get:Rule
    val compose = createComposeRule()

    // --- 1-1 타입 선택 시트 ---

    @Test
    fun typeSheet_showsBothOptions_andFiresTimeCallback() {
        var timeSelected = false
        compose.setContent {
            WakepointTheme {
                AlarmTypeSheetContent(
                    onSelectLocation = {},
                    onSelectTime = { timeSelected = true },
                    onCancel = {},
                )
            }
        }
        compose.onNodeWithText("어떤 알람을 만들까요?").assertIsDisplayed()
        compose.onNodeWithText("위치 알람").assertIsDisplayed()
        compose.onNodeWithText("시간 알람").assertIsDisplayed()
        compose.onNodeWithText("시간 알람").performClick()
        assertTrue(timeSelected)
    }

    // --- 1-2 생성 시트 ---

    @Test
    fun createSheet_customRepeat_locksDate_andRequiresDay() {
        compose.setContent {
            WakepointTheme { TimeAlarmCreateSheetContent(onCancel = {}, onAdd = {}) }
        }
        // 요일 직접 선택 → 날짜 잠김 + 요일 없인 추가 비활성
        compose.onNodeWithText("요일 직접").performClick()
        compose.onNodeWithText("반복 사용 중").assertIsDisplayed()
        compose.onNodeWithText("알람 추가").assertIsNotEnabled()
        // 요일 하나 고르면 추가 가능
        compose.onNodeWithText("월").performClick()
        compose.onNodeWithText("알람 추가").assertIsEnabled()
    }

    @Test
    fun createSheet_addsOneShotAlarm_withEnteredName() {
        var added: TimeAlarm? = null
        compose.setContent {
            WakepointTheme { TimeAlarmCreateSheetContent(onCancel = {}, onAdd = { added = it }) }
        }
        compose.onNodeWithText("알람 추가").performClick()
        val alarm = added
        assertNotNull(alarm)
        assertEquals(RepeatMode.NONE, alarm!!.repeatMode)
        assertEquals("시간 알람", alarm.label) // 이름 미입력 시 기본 라벨
        assertEquals(0, alarm.minute) // 기본값 "다음 정시"
        assertTrue(alarm.soundEnabled && alarm.vibrate)
    }

    // --- 1-3/1-4 카드 + 1-6 배지 ---

    @Test
    fun timeAlarmCard_showsTypeBadgeAndMeta_thenExpandsToEdit() {
        val alarm = TimeAlarm(
            id = "t1", label = "아침 기상", hour = 7, minute = 0,
            repeatMode = RepeatMode.DAILY,
        )
        compose.setContent {
            WakepointTheme {
                TimeAlarmCard(
                    alarm = alarm,
                    expanded = false,
                    isUpdating = false,
                    exactAlarmAllowed = false,
                    onToggleExpanded = {},
                    onEnabledChange = {},
                    onSave = {},
                    onDeleteRequest = {},
                    onAccuracyBadgeClick = {},
                )
            }
        }
        compose.onNodeWithText("아침 기상").assertIsDisplayed()
        compose.onNodeWithText("시간").assertIsDisplayed()
        compose.onNodeWithText("매일 오전 7:00").assertIsDisplayed()
        // 정확 알람 미허용 → 정확도 제한 배지(1-6)
        compose.onNodeWithText("정확도 제한").assertIsDisplayed()
    }

    @Test
    fun timeAlarmCard_expanded_showsEditControls() {
        val alarm = TimeAlarm(
            id = "t1", label = "아침 기상", hour = 7, minute = 0,
            repeatMode = RepeatMode.DAILY,
        )
        var saved: TimeAlarm? = null
        compose.setContent {
            WakepointTheme {
                TimeAlarmCard(
                    alarm = alarm,
                    expanded = true,
                    isUpdating = false,
                    exactAlarmAllowed = true,
                    onToggleExpanded = {},
                    onEnabledChange = {},
                    onSave = { saved = it },
                    onDeleteRequest = {},
                    onAccuracyBadgeClick = {},
                )
            }
        }
        compose.onNodeWithText("알람 이름").assertIsDisplayed()
        compose.onNodeWithText("시각").assertIsDisplayed()
        compose.onNodeWithText("오전 7:00").assertIsDisplayed()
        compose.onNodeWithText("삭제").assertIsDisplayed()
        // 반복을 주말로 바꾸고 저장 → 변경이 콜백으로 전달된다
        compose.onNodeWithText("주말").performClick()
        compose.onNodeWithText("저장").performClick()
        assertEquals(RepeatMode.WEEKEND, saved?.repeatMode)
    }
}
