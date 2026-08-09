package com.wakepoint.app.e2e.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.feature.v2.lasttrain.LastTrainCard
import com.wakepoint.app.feature.v2.lasttrain.V2LastTrain
import com.wakepoint.app.feature.v2.model.LastTrainLevel
import com.wakepoint.app.feature.v2.timealarm.AlarmTypeSheet
import com.wakepoint.app.feature.v2.timealarm.RepeatMode
import com.wakepoint.app.feature.v2.timealarm.TimeAlarmCreateSheet
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * v2 화면 컴포넌트 E2E(UI 관통) 테스트.
 *
 * v2 화면은 아직 실제 내비게이션에 배선되지 않아(승인 대기 항목) MainActivity 관통 대신
 * 각 자립 Composable 을 [createComposeRule] 로 호스팅해 상호작용을 end-to-end 로 검증한다.
 * 계측 테스트라 실행에는 에뮬레이터/기기가 필요하다(디바이스 없는 CI 에선 컴파일만 보장).
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class V2ScreenE2ETest {

    @get:Rule
    val compose = createComposeRule()

    // ---------- 1a 알람 타입 선택 시트 ----------

    @Test
    fun alarmTypeSheet_showsBothOptions_andRoutesTimeSelection() {
        var selected: String? = null
        compose.setContent {
            WakepointTheme {
                AlarmTypeSheet(
                    onSelectLocation = { selected = "location" },
                    onSelectTime = { selected = "time" },
                )
            }
        }

        compose.onNodeWithText("위치 알람").assertIsDisplayed()
        compose.onNodeWithText("시간 알람").assertIsDisplayed()
        compose.onNodeWithText("정한 시각에 울려요").assertIsDisplayed()

        compose.onNodeWithText("시간 알람").performClick()
        assertEquals("time", selected)
    }

    // ---------- 1c [알람 추가] 버튼 활성 규칙이 UI 로 관통되는지 ----------

    @Test
    fun timeAlarmCreate_addButtonEnabled_whenFutureTimeAndNoRepeat() {
        compose.setContent {
            WakepointTheme {
                TimeAlarmCreateSheet(initialRepeat = RepeatMode.NONE, timeIsPast = false)
            }
        }
        compose.onNodeWithText("알람 추가").assertIsEnabled()
    }

    @Test
    fun timeAlarmCreate_addButtonDisabled_whenTimeIsPast() {
        compose.setContent {
            WakepointTheme {
                TimeAlarmCreateSheet(initialRepeat = RepeatMode.NONE, timeIsPast = true)
            }
        }
        compose.onNodeWithText("알람 추가").assertIsNotEnabled()
    }

    @Test
    fun timeAlarmCreate_addButtonDisabled_whenCustomRepeatWithNoDaySelected() {
        compose.setContent {
            WakepointTheme {
                TimeAlarmCreateSheet(initialRepeat = RepeatMode.CUSTOM, timeIsPast = false)
            }
        }
        // "요일 직접"이지만 요일 미선택 → 비활성(TimeAlarmLogic.isAddEnabled)
        compose.onNodeWithText("알람 추가").assertIsNotEnabled()
    }

    // ---------- 4a 막차 3단계: 단계별 주 액션 전환 ----------

    @Test
    fun lastTrainCard_relaxed_showsSetAlarmAction() {
        compose.setContent {
            WakepointTheme {
                LastTrainCard(
                    V2LastTrain(
                        remaining = "23분",
                        deadline = "선릉역 23:02",
                        bottleneck = "도보 8분 + 환승 대기",
                        taxiFare = null,
                        level = LastTrainLevel.RELAXED,
                    )
                )
            }
        }
        compose.onNodeWithText("선릉역 23:02에 알람 걸기").assertIsDisplayed()
    }

    @Test
    fun lastTrainCard_critical_replacesAlarmWithRouteView() {
        compose.setContent {
            WakepointTheme {
                LastTrainCard(
                    V2LastTrain(
                        remaining = "-4분",
                        deadline = "선릉역 23:02",
                        bottleneck = "막차가 곧 끊겨요",
                        taxiFare = "약 28,000원",
                        level = LastTrainLevel.CRITICAL,
                    )
                )
            }
        }
        // 아슬 단계: 알람이 아니라 "경로 보기"로 교체 + 안내 문구
        compose.onNodeWithText("알람은 이미 늦었어요").assertIsDisplayed()
        compose.onNodeWithText("경로 보기").assertIsDisplayed()
    }
}
