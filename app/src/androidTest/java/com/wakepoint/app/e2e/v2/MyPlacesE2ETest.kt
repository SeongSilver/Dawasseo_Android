package com.wakepoint.app.e2e.v2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.domain.model.Place
import com.wakepoint.app.feature.place.MyPlacesContent
import com.wakepoint.app.feature.place.MyPlacesUiState
import com.wakepoint.app.feature.place.PlaceDeleteDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E(계측): v2 STEP2 내 장소 — 목록(2-1)·삭제 확인(2-4)을 실제 렌더링·상호작용으로 검증.
 * (등록·편집 화면은 Hilt VM 결합이라 규칙은 PlaceRulesTest 유닛으로 커버.)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MyPlacesE2ETest {

    @get:Rule
    val compose = createComposeRule()

    private fun place(
        myName: String,
        isHome: Boolean = false,
        isPublic: Boolean = false,
        sharedName: String? = null,
    ) = Place(
        id = myName, myName = myName, address = "서울", latitude = 37.0, longitude = 127.0,
        isHome = isHome, isPublic = isPublic, sharedName = sharedName,
    )

    @Test
    fun list_showsHomeFirst_publicLabel_andAddSlot() {
        var addHome = false
        var edited: Place? = null
        compose.setContent {
            WakepointTheme {
                MyPlacesContent(
                    uiState = MyPlacesUiState(
                        isLoading = false,
                        places = listOf(
                            place("집", isHome = true),
                            place("여자친구집", isPublic = true, sharedName = "장소 2"),
                        ),
                    ),
                    onBack = {},
                    onAddPlace = { addHome = it },
                    onEditPlace = { edited = it },
                )
            }
        }
        // 슬롯 카운트 + 고정 배지 + 공개 문구
        compose.onNodeWithText("2 / 4 슬롯 사용 중 · 주소는 친구에게 공개되지 않아요").assertIsDisplayed()
        compose.onNodeWithText("고정").assertIsDisplayed()
        compose.onNodeWithText("공개 중 · 친구에겐 '장소 2'로 보여요").assertIsDisplayed()
        compose.onNodeWithText("비공개 · 나만 볼 수 있어요").assertIsDisplayed()
        // 남은 슬롯 → 장소 추가 행
        compose.onNodeWithText("장소 추가").assertIsDisplayed().performClick()
        assertEquals(false, addHome)
        // 장소 탭 → 편집 콜백
        compose.onNodeWithText("여자친구집").performClick()
        assertEquals("여자친구집", edited?.myName)
    }

    @Test
    fun list_withoutHome_promptsHomeRegistration() {
        var addHome = false
        compose.setContent {
            WakepointTheme {
                MyPlacesContent(
                    uiState = MyPlacesUiState(isLoading = false, places = emptyList()),
                    onBack = {},
                    onAddPlace = { addHome = it },
                    onEditPlace = {},
                )
            }
        }
        compose.onNodeWithText("집 등록 (막차 알람에 쓰여요)").assertIsDisplayed().performClick()
        assertTrue(addHome)
    }

    @Test
    fun deleteDialog_mentionsSharedName_andConfirms() {
        var deleted = false
        compose.setContent {
            WakepointTheme {
                PlaceDeleteDialog(
                    placeName = "여자친구집",
                    sharedName = "장소 2",
                    onCancel = {},
                    onDelete = { deleted = true },
                )
            }
        }
        compose.onNodeWithText("'여자친구집'을(를) 삭제할까요?").assertIsDisplayed()
        compose.onNodeWithText("이 장소로 받은 알람도 함께 사라져요.\n친구 목록의 '장소 2'도 사라져요.")
            .assertIsDisplayed()
        compose.onNodeWithText("삭제").performClick()
        assertTrue(deleted)
    }
}
