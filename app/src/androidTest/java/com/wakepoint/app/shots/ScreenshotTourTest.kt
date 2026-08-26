package com.wakepoint.app.shots

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.wakepoint.app.MainActivity
import com.wakepoint.app.R
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.notification.AlarmAlertContract
import com.wakepoint.app.data.auth.AuthSession
import com.wakepoint.app.feature.alarmalert.AlarmAlertActivity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * 화면별 스크린샷 추출용 투어(테스트가 아니라 캡처 도구).
 *
 * 각 화면에 도달한 뒤 uiAutomation 으로 실제 화면(지도 SurfaceView 포함)을 캡처해
 * 앱 외부 저장소(files/screenshots)에 PNG 로 저장한다. 실행 후 adb pull 로 회수한다.
 *
 * 가짜 세션으로 진입하므로 서버 호출은 401 로 실패한다 — 각 화면에서 에러 다이얼로그가
 * 뜨면 "확인"으로 닫은 뒤 캡처한다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenshotTourTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private var scenario: ActivityScenario<*>? = null
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private fun string(resId: Int, vararg args: Any): String =
        if (args.isEmpty()) context.getString(resId) else context.getString(resId, *args)

    @After
    fun tearDown() {
        scenario?.close()
        runBlocking { UserPreferencesDataStore(context).clearAuthSession() }
    }

    /** 1) 비로그인: 스플래시(베스트에포트) → 로그인 → 회원가입 */
    @Test
    fun tour1_authScreens() {
        runBlocking { UserPreferencesDataStore(context).clearAuthSession() }
        grantRuntimePermissions()
        markAlarmDisplayPrompted()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        // 스플래시는 세션 판정 즉시 사라지므로 베스트에포트로 즉시 1장
        capture("01_splash")

        waitForText(string(R.string.auth_intro))
        settle()
        capture("02_login")

        clickText(string(R.string.auth_signup))
        waitForText(string(R.string.field_name))
        settle()
        capture("03_signup")
    }

    /** 2) 로그인 상태: 지도 홈 → 알람 설정 시트 → 알람 목록 → 친구 → 마이 → 사운드 목록 */
    @Test
    fun tour2_mainScreens() {
        grantRuntimePermissions()
        markAlarmDisplayPrompted()
        runBlocking { UserPreferencesDataStore(context).saveAuthSession(fakeSession()) }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText(string(R.string.tab_alarms))
        waitForText(string(R.string.home_search_placeholder))
        Thread.sleep(6_000) // 지도 타일 로딩 대기
        dismissDialogs()
        capture("04_home_map")

        // 알람 설정 바텀시트
        clickText(string(R.string.home_create_alarm))
        waitForText(string(R.string.alarm_setup_title))
        settle()
        capture("05_alarm_setup_sheet")

        // 저장 → 알람 탭으로 이동(알람 1개 생성됨)
        clickText(string(R.string.alarm_add))
        waitForText(string(R.string.alarms_summary_active, 1), timeoutMs = 20_000)
        dismissDialogs()
        capture("06_alarms")

        clickText(string(R.string.tab_friends))
        waitForText(string(R.string.friends_subtitle))
        dismissDialogs()
        settle()
        capture("07_friends")

        clickText(string(R.string.tab_profile))
        waitForText(string(R.string.profile_sound))
        dismissDialogs()
        settle()
        capture("08_profile")

        clickText(string(R.string.profile_sound))
        waitForText(string(R.string.sound_section_bundled))
        settle()
        capture("09_sound_list")
    }

    /** 3) 도착 알람 전체화면 (직접 실행 — 소리/진동은 서비스가 담당하므로 무음) */
    @Test
    fun tour3_alarmAlert() {
        val intent = Intent(context, AlarmAlertActivity::class.java)
            .putExtra(AlarmAlertContract.EXTRA_ALARM_LABEL, "강남역")
            .putExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS, "서울 강남구 강남대로 396")
        scenario = ActivityScenario.launch<AlarmAlertActivity>(intent)
        waitForText(string(R.string.alarm_alert_arrived))
        settle()
        capture("10_alarm_alert")
    }

    // ---------- helpers ----------

    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val dir = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun settle(ms: Long = 1_200) = Thread.sleep(ms)

    private fun clickText(text: String) {
        compose.onAllNodesWithText(text).onFirst().performClick()
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMillis = timeoutMs) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** 서버 401 등으로 뜬 에러 다이얼로그("확인")를 모두 닫는다. */
    private fun dismissDialogs() {
        repeat(3) {
            val buttons = compose.onAllNodesWithText("확인").fetchSemanticsNodes()
            if (buttons.isEmpty()) return
            compose.onAllNodesWithText("확인").onFirst().performClick()
            Thread.sleep(400)
        }
    }

    private fun markAlarmDisplayPrompted() {
        context.getSharedPreferences("wakepoint_prompts", Context.MODE_PRIVATE)
            .edit().putBoolean("alarm_display_prompted", true).commit()
    }

    private fun grantRuntimePermissions() {
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
                instrumentation.uiAutomation.grantRuntimePermission(context.packageName, permission)
            }
        }
    }

    private fun fakeSession(): AuthSession = AuthSession(
        userId = "00000000-0000-0000-0000-000000000009",
        email = "shots@wakepoint.test",
        nickname = "다왔어",
        accessToken = "screenshot-fake-token",
        refreshToken = "screenshot-fake-refresh",
        expiresAtEpochSeconds = Instant.now().epochSecond + 86_400L
    )
}
