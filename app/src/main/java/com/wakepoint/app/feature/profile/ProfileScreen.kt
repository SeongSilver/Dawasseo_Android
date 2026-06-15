package com.wakepoint.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDarkTile
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.data.mock.MockWakepointData

@Composable
fun ProfileScreen() {
    val user = MockWakepointData.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = WakepointDarkTile
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = user.nickname,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = user.email,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        WakepointCard {
            SettingText(
                title = stringResource(R.string.profile_location_permission),
                body = stringResource(R.string.profile_location_permission_body)
            )
        }
        WakepointCard {
            SettingText(
                title = stringResource(R.string.profile_notification_permission),
                body = stringResource(R.string.profile_notification_permission_body)
            )
        }
        WakepointCard {
            SettingText(
                title = stringResource(R.string.profile_legal_documents),
                body = stringResource(R.string.profile_legal_documents_body)
            )
        }
    }
}

@Composable
private fun SettingText(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = WakepointMuted)
    }
}
