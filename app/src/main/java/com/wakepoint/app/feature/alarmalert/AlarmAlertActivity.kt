package com.wakepoint.app.feature.alarmalert

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointLogo
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
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WakepointLogo()
                Text(
                    text = stringResource(R.string.alarm_alert_arrived),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                if (targetAddress.isNotBlank() && targetAddress != locationName) {
                    Text(
                        text = targetAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                WakepointButton(
                    text = stringResource(R.string.alarm_alert_stop),
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
