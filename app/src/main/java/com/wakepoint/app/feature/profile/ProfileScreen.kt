package com.wakepoint.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.wakepoint.app.R
import com.wakepoint.app.core.design.SettingsRow
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointIconLight
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointTextDisabled
import com.wakepoint.app.core.design.WakepointTextSecondary

@Composable
fun ProfileScreen(
    email: String,
    onOpenSoundList: () -> Unit,
    onLogout: () -> Unit
) {
    val profileName = stringResource(R.string.profile_name)
    val profileEmail = email.ifBlank { stringResource(R.string.profile_email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground)
    ) {
        WakepointPageHeader(title = stringResource(R.string.profile_title))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileCard(name = profileName, email = profileEmail)
            StatsCard()
            SettingsGroupCard(onOpenSoundList = onOpenSoundList)
            WakepointCard(contentPadding = 0) {
                SettingsRow(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = stringResource(R.string.profile_logout),
                    danger = true,
                    showChevron = false,
                    onClick = onLogout
                )
            }
            Text(
                text = stringResource(R.string.profile_version),
                style = MaterialTheme.typography.bodySmall,
                color = WakepointTextDisabled,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ProfileCard(name: String, email: String) {
    WakepointCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WakepointAvatar(name = name, size = 58)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = WakepointInk
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = WakepointIconLight
            )
        }
    }
}

@Composable
private fun StatsCard() {
    WakepointCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                value = "128",
                label = stringResource(R.string.profile_stat_wakeups),
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatColumn(
                value = "3",
                label = stringResource(R.string.profile_stat_active),
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatColumn(
                value = "4",
                label = stringResource(R.string.profile_stat_friends),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WakepointTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(WakepointDivider)
    )
}

@Composable
private fun SettingsGroupCard(onOpenSoundList: () -> Unit) {
    WakepointCard(contentPadding = 0) {
        SettingsRow(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.profile_notification)
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = 54.dp))
        SettingsRow(
            icon = Icons.Rounded.LocationOn,
            title = stringResource(R.string.profile_location)
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = 54.dp))
        SettingsRow(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            title = stringResource(R.string.profile_sound),
            onClick = onOpenSoundList
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = 54.dp))
        SettingsRow(
            icon = Icons.AutoMirrored.Rounded.HelpOutline,
            title = stringResource(R.string.profile_support)
        )
    }
}
