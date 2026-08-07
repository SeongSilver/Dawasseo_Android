package com.wakepoint.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.wakepoint.app.MainActivity
import com.wakepoint.app.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E: 화면 전환/뒤로가기 왕복 — 인증 화면 -> 회원가입 -> 시스템 뒤로가기 -> 인증 화면.
 *
 * Navigation Compose 백스택(popBackStack)이 정상 동작하는지 검증한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SignUpNavigationE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(resId: Int): String = context.getString(resId)

    @Before
    fun setUp() {
        // 초기 상태 리셋: 인증 세션/설정 삭제 후 비로그인 상태로 시작
        context.filesDir.resolve("datastore").deleteRecursively()
        context.dataDir.resolve("shared_prefs").deleteRecursively()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun authToSignUp_andBack_returnsToAuth() {
        // 1) 인증 화면 진입 대기
        waitForText(string(R.string.auth_intro))

        // 2) "회원가입" 링크 클릭 -> 회원가입 화면 진입
        compose.onNodeWithText(string(R.string.auth_signup)).performClick()
        waitForText(string(R.string.signup_name_hint))
        compose.onNodeWithText(string(R.string.signup_name_hint)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.signup_email_hint)).assertIsDisplayed()

        // 3) 시스템 뒤로가기 -> 인증 화면으로 복귀 (popBackStack)
        Espresso.pressBack()
        waitForText(string(R.string.auth_intro))
        compose.onNodeWithText(string(R.string.auth_intro)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.auth_signup)).assertIsDisplayed()
    }
}
