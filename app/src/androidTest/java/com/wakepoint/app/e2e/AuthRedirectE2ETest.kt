package com.wakepoint.app.e2e

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.wakepoint.app.MainActivity
import com.wakepoint.app.R
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.data.auth.AuthSession
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E: 로그인 상태 진입 후, 초기 로딩이 끝나기 전에 한 탭 이동이 유지되는지 검증.
 *
 * 회귀 방지: 인증 라우팅 effect 가 조건 없이 navigate(Home) 을 호출해,
 * 초기 로딩(백그라운드 세션 검증·동기화) 완료나 액티비티 재생성 시점에
 * 사용자가 보던 화면을 지도 첫 화면으로 강제로 되돌리던 문제.
 *
 * 저장된 세션(DataStore)을 미리 심어 네트워크 없이 로그인 상태로 시작하고,
 * 런타임 권한을 미리 부여해 시스템 다이얼로그 간섭을 제거한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthRedirectE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(resId: Int): String = context.getString(resId)

    @Before
    fun setUp() {
        grantRuntimePermissions()
        runBlocking {
            UserPreferencesDataStore(context).saveAuthSession(fakeSession())
        }
        // 전체화면 권한 안내 다이얼로그가 탭 조작을 가리지 않도록 이미 안내된 상태로 시작
        context.getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ALARM_DISPLAY_PROMPTED, true)
            .commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        runBlocking { UserPreferencesDataStore(context).clearAuthSession() }
        context.getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun tabSelection_survivesInitialLoadAndActivityRecreation() {
        // 저장된 세션으로 스플래시를 지나 홈(지도)에 도착
        waitForText(string(R.string.tab_alarms))
        waitForText(string(R.string.home_search_placeholder))

        // 초기 로딩(백그라운드 동기화)이 끝나기 전에 알람 탭으로 이동
        compose.onNodeWithText(string(R.string.tab_alarms)).performClick()
        waitForContentDescription(string(R.string.alarm_add))

        // 백그라운드 세션 검증·알람 동기화가 끝난 뒤에도 알람 화면에 그대로 있어야 한다
        Thread.sleep(3_000)
        compose.onAllNodesWithContentDescription(string(R.string.alarm_add))
            .onFirst().assertIsDisplayed()
        compose.onAllNodesWithText(string(R.string.home_search_placeholder))
            .assertCountEquals(0)

        // 액티비티 재생성(다크모드 전환, 권한 설정 왕복 등) 후에도 알람 화면이 유지되어야 한다
        scenario.recreate()
        waitForContentDescription(string(R.string.alarm_add))
        compose.onAllNodesWithText(string(R.string.home_search_placeholder))
            .assertCountEquals(0)
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForContentDescription(description: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            compose.onAllNodesWithContentDescription(description)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun grantRuntimePermissions() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
        permissions.forEach { permission ->
            runCatching {
                uiAutomation.grantRuntimePermission(context.packageName, permission)
            }
        }
    }

    private fun fakeSession(): AuthSession = AuthSession(
        userId = "00000000-0000-0000-0000-000000000001",
        email = "e2e@wakepoint.test",
        nickname = "E2E테스터",
        accessToken = "e2e-fake-access-token",
        refreshToken = "e2e-fake-refresh-token",
        // 만료가 충분히 멀어 refreshSessionIfNeeded 가 네트워크를 타지 않는다
        expiresAtEpochSeconds = Instant.now().epochSecond + 86_400L
    )

    private companion object {
        const val PROMPT_PREFS = "wakepoint_prompts"
        const val KEY_ALARM_DISPLAY_PROMPTED = "alarm_display_prompted"
    }
}
