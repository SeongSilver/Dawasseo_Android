package com.wakepoint.app.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointHeader

@Composable
fun ProfileScreen(
    email: String,
    onLogout: () -> Unit
) {
    val profileName = stringResource(R.string.profile_name)
    val profileEmail = email.ifBlank { stringResource(R.string.profile_email) }

    Column(modifier = Modifier.fillMaxSize()) {
        WakepointHeader()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profileName.take(1),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = profileEmail,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider()
        ProfileMenuItem(
            icon = Icons.Rounded.Settings,
            text = stringResource(R.string.profile_setting)
        )
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Rounded.HelpOutline,
            text = stringResource(R.string.profile_support)
        )
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Rounded.Logout,
            text = stringResource(R.string.profile_logout),
            showChevron = false,
            onClick = onLogout
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ListItem(
            modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            headlineContent = { Text(text = text) },
            leadingContent = { Icon(imageVector = icon, contentDescription = null) },
            trailingContent = if (showChevron) {
                { Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null) }
            } else {
                null
            }
        )
        HorizontalDivider()
    }
}
