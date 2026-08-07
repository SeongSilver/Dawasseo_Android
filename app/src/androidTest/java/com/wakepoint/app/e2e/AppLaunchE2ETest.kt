package com.wakepoint.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
 * E2E: 앱 실행 -> 스플래시 -> (비로그인) 인증 화면 진입.
 *
 * 저장된 세션이 없으면 Splash 이후 Auth 화면으로 라우팅되어야 한다.
 * 네트워크 호출 없이 검증 가능한 순수 진입 플로우만 다룬다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppLaunchE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(resId: Int): String = context.getString(resId)

    @Before
    fun setUp() {
        // 초기 상태 리셋: 저장된 인증 세션(DataStore)과 권한 안내 플래그(SharedPreferences)를
        // 지워 항상 비로그인 상태로 시작한다.
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
    fun launch_withoutSession_landsOnAuthScreen() {
        // 스플래시가 지나가고 인증 화면 고유 문구가 표시될 때까지 대기
        waitForText(string(R.string.auth_intro))

        compose.onNodeWithText(string(R.string.auth_intro)).assertIsDisplayed()
        // 이메일 입력 필드(placeholder)와 로그인 버튼이 함께 보인다
        compose.onNodeWithText(string(R.string.field_email)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.auth_login)).assertIsDisplayed()
        // 회원가입 진입 링크가 존재한다
        compose.onNodeWithText(string(R.string.auth_signup)).assertIsDisplayed()
    }
}
