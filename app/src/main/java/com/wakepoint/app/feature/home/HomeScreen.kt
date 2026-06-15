package com.wakepoint.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.MapMarkerPreview
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSuccess
import com.wakepoint.app.data.mock.MockWakepointData

@Composable
fun HomeScreen() {
    val activeAlarm = MockWakepointData.alarms.first { it.isActive }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointParchment)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = Color.White
        ) {
            Text(
                text = stringResource(R.string.home_search_placeholder),
                color = WakepointMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            MapMarkerPreview()
            Text(
                text = stringResource(R.string.home_map_preview),
                color = WakepointMuted,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        WakepointCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(
                        text = stringResource(R.string.home_active_status),
                        color = WakepointSuccess
                    )
                    Text(
                        text = activeAlarm.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.home_alarm_summary,
                            activeAlarm.targetAddress,
                            activeAlarm.radiusKm
                        ),
                        color = WakepointMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = WakepointPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "+", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        WakepointButton(
            text = stringResource(R.string.home_create_alarm),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
