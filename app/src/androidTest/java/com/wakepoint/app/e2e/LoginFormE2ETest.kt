package com.wakepoint.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
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
 * E2E: 로그인 폼 입력 1회 완주 — 이메일/비밀번호 입력 후 로그인 버튼 활성 상태 확인.
 *
 * 실제 로그인 제출(네트워크: Supabase Auth)은 Fake AuthRepository 또는
 * MockWebServer 주입이 선행되어야 하므로 여기서는 제출 직전까지의 폼 플로우만
 * 검증한다. (TASKS_NOW.md Blocked 15번 참조)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginFormE2ETest {

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
    fun fillLoginForm_keepsInputAndEnablesSubmit() {
        val email = "e2e-user@example.com"
        val password = "secret-password-1"

        // 1) 인증 화면 진입 대기
        waitForText(string(R.string.auth_intro))

        // 2) 이메일 입력: placeholder 노드가 곧 텍스트 필드다
        compose.onNodeWithText(string(R.string.field_email)).performTextInput(email)
        // 입력값이 화면에 그대로 반영된다
        compose.onNodeWithText(email).assertIsDisplayed()

        // 3) 비밀번호 입력: 입력 후 placeholder 는 사라진다 (마스킹되어 원문은 노출되지 않음)
        compose.onNodeWithText(string(R.string.auth_password)).performTextInput(password)
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(string(R.string.auth_password))
                .fetchSemanticsNodes().isEmpty()
        }

        // 4) 로그인 버튼이 표시되고 활성 상태다 (제출은 네트워크 의존이라 수행하지 않음)
        compose.onNodeWithText(string(R.string.auth_login))
            .assertIsDisplayed()
            .assertIsEnabled()
    }
}
