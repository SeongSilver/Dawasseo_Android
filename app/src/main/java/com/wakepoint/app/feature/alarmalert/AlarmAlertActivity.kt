package com.wakepoint.app.feature.alarmalert

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.wakepoint.app.R
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointButtonSize
import com.wakepoint.app.core.design.WakepointLogo
import com.wakepoint.app.core.design.WakepointSize
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.notification.AlarmAlertContract
import com.wakepoint.app.core.notification.AlarmVibrationController
import com.wakepoint.app.service.AlarmAlertService

class AlarmAlertActivity : ComponentActivity() {
    private val alarmId: String
        get() = intent.getStringExtra(AlarmAlertContract.EXTRA_ALARM_ID).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge()

        val label = intent.getStringExtra(AlarmAlertContract.EXTRA_ALARM_LABEL)
            ?: getString(R.string.alarm_alert_default_location)
        val targetAddress = intent.getStringExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS).orEmpty()

        setContent {
            WakepointTheme {
                AlarmAlertScreen(
                    locationName = label,
                    targetAddress = targetAddress,
                    onStop = ::stopAlarmAndClose
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun stopAlarmAndClose() {
        AlarmVibrationController.stop()
        stopService(AlarmAlertService.stopIntent(this))
        if (alarmId.isNotBlank()) {
            NotificationManagerCompat.from(this).cancel(
                AlarmAlertContract.notificationId(alarmId)
            )
        }
        finishAndRemoveTask()
    }

    private fun Activity.showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    companion object {
        fun launchIntent(
            context: Context,
            alarmId: String,
            label: String,
            targetAddress: String
        ): Intent {
            return Intent(context, AlarmAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AlarmAlertContract.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmAlertContract.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS, targetAddress)
            }
        }
    }
}


/**
 * 알람 울림 화면 (design-handoff `Ringing` · `Ringing-alias`, 명세 9절).
 *
 * 잠금화면 위에 뜨므로 "알람 끄기" 버튼 하나가 가장 크게 보여야 한다.
 * 펄스 원 208 / 152 / 96 (`fillWeak` · `fill` · `fillStrong`), 중지 버튼 312×96.
 * 테마는 DESIGN.md 8절 기본값대로 시스템 설정을 따른다.
 */
@Composable
private fun AlarmAlertScreen(
    locationName: String,
    targetAddress: String,
    onStop: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WakepointLogo(modifier = Modifier.padding(top = WakepointSpacing.xl))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = WakepointSpacing.formHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xxl, Alignment.CenterVertically)
        ) {
            PulseRings()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.alarm_alert_arrived),
                    color = colors.text,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = locationName,
                    color = colors.text,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                if (targetAddress.isNotBlank() && targetAddress != locationName) {
                    Text(
                        text = targetAddress,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        WakepointButton(
            text = stringResource(R.string.alarm_alert_stop),
            onClick = onStop,
            size = WakepointButtonSize.Large,
            modifier = Modifier
                .padding(
                    start = WakepointSpacing.formHorizontal,
                    end = WakepointSpacing.formHorizontal,
                    bottom = WakepointSpacing.xxxl
                )
                .width(WakepointSize.stopButtonWidth)
        )
    }
}

/** 울림 펄스 — 바깥부터 208 `fillWeak` / 152 `fill` / 96 `fillStrong`, 가운데 alarm 아이콘 40. */
@Composable
private fun PulseRings() {
    val colors = LocalWakepointColors.current
    Box(
        modifier = Modifier
            .size(RING_OUTER)
            .background(colors.fillWeak, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(RING_MIDDLE)
                .background(colors.fill, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(RING_INNER)
                    .background(colors.fillStrong, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = colors.text,
                    modifier = Modifier.size(RING_ICON)
                )
            }
        }
    }
}

// 펄스 원과 그 안 아이콘은 디자인 고유의 구조적 치수라 스페이싱 토큰 대상이 아니다.
private val RING_OUTER = 208.dp
private val RING_MIDDLE = 152.dp
private val RING_INNER = 96.dp
private val RING_ICON = 40.dp

@Preview(name = "Ringing · light", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AlarmAlertScreenLightPreview() {
    WakepointTheme(darkTheme = false) {
        AlarmAlertScreen(locationName = "회사", targetAddress = "서울 강남구 테헤란로 152", onStop = {})
    }
}

@Preview(
    name = "Ringing · dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AlarmAlertScreenDarkPreview() {
    WakepointTheme(darkTheme = true) {
        AlarmAlertScreen(locationName = "회사", targetAddress = "서울 강남구 테헤란로 152", onStop = {})
    }
}

@Preview(name = "Ringing-alias · light", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AlarmAlertScreenAliasLightPreview() {
    WakepointTheme(darkTheme = false) {
        AlarmAlertScreen(locationName = "회사", targetAddress = "", onStop = {})
    }
}

@Preview(
    name = "Ringing-alias · dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 915,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AlarmAlertScreenAliasDarkPreview() {
    WakepointTheme(darkTheme = true) {
        AlarmAlertScreen(locationName = "회사", targetAddress = "", onStop = {})
    }
}
