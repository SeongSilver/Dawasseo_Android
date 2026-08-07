package com.wakepoint.app.e2e

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E: 전체화면 알람 권한 안내 다이얼로그가 사용자의 응답("권한 설정"/"나중에") 이후
 * 앱을 다시 실행해도 반복해서 뜨지 않는지 검증.
 *
 * 회귀 방지: "권한 설정"으로 이동한 경우 영구 플래그가 저장되지 않아, 미허용 권한이
 * 남아 있으면 매 실행마다 전체화면 권한을 무한 반복 요청하던 문제.
 *
 * 기기에 이미 두 권한(전체 화면 알림·다른 앱 위에 표시)이 모두 허용돼 있으면
 * 다이얼로그 자체가 뜨지 않으므로 테스트를 건너뛴다(assumeTrue).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AlarmDisplayPromptE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(resId: Int): String = context.getString(resId)

    @Before
    fun setUp() {
        assumeTrue("전체화면/오버레이 권한이 이미 모두 허용됨 — 안내 다이얼로그 시나리오 불가", needsAlarmDisplayPermission())
        grantRuntimePermissions()
        runBlocking {
            UserPreferencesDataStore(context).saveAuthSession(fakeSession())
        }
        // 안내 이력이 없는 상태에서 시작
        context.getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        runBlocking { UserPreferencesDataStore(context).clearAuthSession() }
        context.getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun confirm_thenRelaunch_doesNotPromptAgain() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText(string(R.string.perm_alarm_display_title))

        // "권한 설정" 선택 → 설정 화면으로 이동하면서 영구 플래그가 저장되어야 한다
        compose.onNodeWithText(string(R.string.perm_alarm_display_confirm)).performClick()
        waitUntilPrompted()

        // 앱 재실행(설정에서 권한을 끝까지 허용하지 않고 돌아온 상황) → 다시 안내하지 않는다
        scenario.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText(string(R.string.tab_alarms))
        Thread.sleep(1_500)
        compose.onAllNodesWithText(string(R.string.perm_alarm_display_title))
            .assertCountEquals(0)
    }

    @Test
    fun dismiss_thenRelaunch_doesNotPromptAgain() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText(string(R.string.perm_alarm_display_title))

        // "나중에" 선택 → 영구 플래그 저장
        compose.onNodeWithText(string(R.string.perm_alarm_display_later)).performClick()
        waitUntilPrompted()

        scenario.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText(string(R.string.tab_alarms))
        Thread.sleep(1_500)
        compose.onAllNodesWithText(string(R.string.perm_alarm_display_title))
            .assertCountEquals(0)
    }

    private fun needsAlarmDisplayPermission(): Boolean {
        val fullScreenMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            context.getSystemService(NotificationManager::class.java)
                ?.canUseFullScreenIntent() == false
        val overlayMissing = !Settings.canDrawOverlays(context)
        return fullScreenMissing || overlayMissing
    }

    private fun waitUntilPrompted(timeoutMs: Long = 5_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            context.getSharedPreferences(PROMPT_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ALARM_DISPLAY_PROMPTED, false)
        }
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
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
        userId = "00000000-0000-0000-0000-000000000002",
        email = "e2e-prompt@wakepoint.test",
        nickname = "E2E테스터",
        accessToken = "e2e-fake-access-token",
        refreshToken = "e2e-fake-refresh-token",
        expiresAtEpochSeconds = Instant.now().epochSecond + 86_400L
    )

    private companion object {
        const val PROMPT_PREFS = "wakepoint_prompts"
        const val KEY_ALARM_DISPLAY_PROMPTED = "alarm_display_prompted"
    }
}
