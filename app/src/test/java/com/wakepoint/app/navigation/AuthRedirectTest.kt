package com.wakepoint.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 인증 라우팅 결정 로직(resolveAuthRedirect) 테스트.
 *
 * 핵심 회귀 방지: "초기 로딩이 끝나기 전에 다른 화면으로 이동하면 지도 첫 화면으로
 * 되돌아가는" 문제. 라우팅 effect 가 늦게(또는 액티비티 재생성으로 다시) 실행돼도
 * 사용자가 이미 올바른 영역에 있으면 절대 이동시키지 않아야 한다.
 */
class AuthRedirectTest {

    @Test
    fun `세션 확인 중에는 이동하지 않는다`() {
        assertNull(
            resolveAuthRedirect(
                isCheckingSession = true,
                isAuthenticated = false,
                currentRoute = "splash"
            )
        )
    }

    @Test
    fun `NavHost 준비 전(currentRoute null)에는 이동하지 않는다`() {
        assertNull(
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = true,
                currentRoute = null
            )
        )
    }

    @Test
    fun `로그인 상태로 스플래시에 있으면 홈으로 보낸다`() {
        assertEquals(
            "home",
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = true,
                currentRoute = "splash"
            )
        )
    }

    @Test
    fun `비로그인 상태로 스플래시에 있으면 로그인 화면으로 보낸다`() {
        assertEquals(
            "auth",
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = false,
                currentRoute = "splash"
            )
        )
    }

    @Test
    fun `로그인 완료 시 로그인 화면과 가입 화면에서는 홈으로 보낸다`() {
        assertEquals(
            "home",
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = true,
                currentRoute = "auth"
            )
        )
        assertEquals(
            "home",
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = true,
                currentRoute = "sign-up"
            )
        )
    }

    @Test
    fun `로그인 상태로 알람 탭에 있으면 이동하지 않는다 - 초기 로딩 중 액션 회귀`() {
        // 초기 로딩(백그라운드 세션 검증)이 끝나거나 액티비티가 재생성돼 effect 가
        // 다시 실행돼도, 이미 메인 영역에 있으면 지도(home)로 되돌리지 않는다.
        assertNull(
            resolveAuthRedirect(
                isCheckingSession = false,
                isAuthenticated = true,
                currentRoute = "alarms"
            )
        )
    }

    @Test
    fun `로그인 상태로 홈·친구·마이 탭과 사운드 목록에서도 이동하지 않는다`() {
        listOf("home", "friends", "profile", "sound-list?alarmId={alarmId}").forEach { route ->
            assertNull(
                "route=$route 에서 이동이 발생함",
                resolveAuthRedirect(
                    isCheckingSession = false,
                    isAuthenticated = true,
                    currentRoute = route
                )
            )
        }
    }

    @Test
    fun `세션이 사라지면(로그아웃·만료) 메인 영역 어디서든 로그인 화면으로 보낸다`() {
        listOf("home", "alarms", "friends", "profile").forEach { route ->
            assertEquals(
                "auth",
                resolveAuthRedirect(
                    isCheckingSession = false,
                    isAuthenticated = false,
                    currentRoute = route
                )
            )
        }
    }

    @Test
    fun `비로그인 상태로 로그인·가입 화면에 있으면 이동하지 않는다`() {
        listOf("auth", "sign-up").forEach { route ->
            assertNull(
                resolveAuthRedirect(
                    isCheckingSession = false,
                    isAuthenticated = false,
                    currentRoute = route
                )
            )
        }
    }
}
