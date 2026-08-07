package com.wakepoint.app.core.permission

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 전체화면 알람 권한 안내 표시 결정 로직(shouldPromptAlarmDisplay) 테스트.
 *
 * 핵심 회귀 방지: "알람 설정 진입 시마다 전체화면 권한을 반복 요청"하던 문제.
 * enabled 가 메인 탭↔서브화면 이동으로 여러 번 토글돼도, 이번 실행에서 한 번
 * 평가한 뒤에는(evaluatedThisSession=true) 다시 안내하지 않아야 한다.
 */
class AlarmDisplayPermissionGateTest {

    @Test
    fun `권한 없고 처음 평가하면 안내한다`() {
        assertTrue(
            shouldPromptAlarmDisplay(
                enabled = true,
                evaluatedThisSession = false,
                dismissedPermanently = false,
                permissionMissing = true
            )
        )
    }

    @Test
    fun `이번 실행에서 이미 평가했으면 반복 안내하지 않는다`() {
        assertFalse(
            shouldPromptAlarmDisplay(
                enabled = true,
                evaluatedThisSession = true,
                dismissedPermanently = false,
                permissionMissing = true
            )
        )
    }

    @Test
    fun `나중에로 영구 억제했으면 안내하지 않는다`() {
        assertFalse(
            shouldPromptAlarmDisplay(
                enabled = true,
                evaluatedThisSession = false,
                dismissedPermanently = true,
                permissionMissing = true
            )
        )
    }

    @Test
    fun `권한이 이미 있으면 안내하지 않는다`() {
        assertFalse(
            shouldPromptAlarmDisplay(
                enabled = true,
                evaluatedThisSession = false,
                dismissedPermanently = false,
                permissionMissing = false
            )
        )
    }

    @Test
    fun `enabled 가 false면 안내하지 않는다`() {
        assertFalse(
            shouldPromptAlarmDisplay(
                enabled = false,
                evaluatedThisSession = false,
                dismissedPermanently = false,
                permissionMissing = true
            )
        )
    }

    @Test
    fun `enabled 가 여러 번 토글돼도 두 번째부터는 안내하지 않는다`() {
        // 첫 진입: 평가 전 → 안내
        val first = shouldPromptAlarmDisplay(
            enabled = true,
            evaluatedThisSession = false,
            dismissedPermanently = false,
            permissionMissing = true
        )
        // 서브화면 이동으로 enabled=false 되었다가 다시 true 로 복귀(이미 평가됨) → 안내 안 함
        val secondEntry = shouldPromptAlarmDisplay(
            enabled = true,
            evaluatedThisSession = true,
            dismissedPermanently = false,
            permissionMissing = true
        )
        val thirdEntry = shouldPromptAlarmDisplay(
            enabled = true,
            evaluatedThisSession = true,
            dismissedPermanently = false,
            permissionMissing = true
        )
        assertTrue(first)
        assertFalse(secondEntry)
        assertFalse(thirdEntry)
    }
}
