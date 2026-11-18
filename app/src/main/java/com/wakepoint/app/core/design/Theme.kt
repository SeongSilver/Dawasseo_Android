package com.wakepoint.app.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 앱 테마 주입 지점.
 * 색상, 타이포그래피, 모양 토큰은 모두 제거된 상태다.
 * 새 디자인은 여기에 colorScheme, typography, shapes를 넘긴다. docs/DESIGN_SPEC.md 참조.
 */
@Composable
fun WakepointTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
