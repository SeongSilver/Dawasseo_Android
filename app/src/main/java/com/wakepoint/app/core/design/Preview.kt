package com.wakepoint.app.core.design

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * 화면 미리보기용 다중 @Preview — 라이트와 다크를 한 번에 본다.
 *
 * 디자인 프레임과 같은 412×915dp 를 쓴다. 테마는 `WakepointTheme()` 기본값
 * (`isSystemInDarkTheme()`)이 `uiMode` 를 따라가므로 따로 넘기지 않아도 된다.
 */
@Preview(name = "light", widthDp = 412, heightDp = 915, showBackground = true)
@Preview(
    name = "dark",
    widthDp = 412,
    heightDp = 915,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class WakepointScreenPreviews

/** 시트·카드처럼 높이가 내용에 따라 달라지는 조각용. */
@Preview(name = "light", widthDp = 412, showBackground = true)
@Preview(
    name = "dark",
    widthDp = 412,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class WakepointComponentPreviews
