package com.wakepoint.app.feature.alarms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointSuccess
import com.wakepoint.app.data.mock.MockWakepointData
import com.wakepoint.app.domain.model.Alarm

@Composable
fun AlarmsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.alarms_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )
        MockWakepointData.alarms.forEach { alarm ->
            AlarmCard(alarm = alarm)
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm) {
    val creatorLabel = if (alarm.createdBy == alarm.ownerId) {
        stringResource(R.string.alarm_created_by_me)
    } else {
        stringResource(R.string.alarm_created_by_friend)
    }

    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = stringResource(
                    if (alarm.isActive) R.string.alarm_active else R.string.alarm_inactive
                ),
                color = if (alarm.isActive) WakepointSuccess else WakepointDanger
            )
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.titleMedium,
                color = WakepointInk
            )
            Text(
                text = alarm.targetAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted
            )
            Text(
                text = stringResource(R.string.alarm_radius, alarm.radiusKm, creatorLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted
            )
        }
    }
}
