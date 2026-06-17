package com.wakepoint.app.feature.alarmalert

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointTheme
import com.wakepoint.app.core.notification.AlarmAlertContract
import com.wakepoint.app.core.notification.AlarmVibrationController

class AlarmAlertActivity : ComponentActivity() {
    private val alarmId: String
        get() = intent.getStringExtra(AlarmAlertContract.EXTRA_ALARM_ID).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

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
        if (alarmId.isNotBlank()) {
            NotificationManagerCompat.from(this).cancel(alarmId.hashCode())
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
}

@Composable
private fun AlarmAlertScreen(
    locationName: String,
    targetAddress: String,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = WakepointCanvas,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_square),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(76.dp)
                )
                Text(
                    text = stringResource(R.string.alarm_alert_arrived),
                    color = WakepointInk,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = locationName,
                    color = WakepointInk,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                if (targetAddress.isNotBlank() && targetAddress != locationName) {
                    Text(
                        text = targetAddress,
                        color = WakepointMuted,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                WakepointButton(
                    text = stringResource(R.string.alarm_alert_stop),
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
