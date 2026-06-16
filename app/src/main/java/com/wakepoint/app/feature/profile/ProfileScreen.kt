package com.wakepoint.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointParchment

@Composable
fun ProfileScreen(
    email: String,
    onLogout: () -> Unit
) {
    val profileName = stringResource(R.string.profile_name)
    val profileEmail = email.ifBlank { stringResource(R.string.profile_email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointCanvas)
    ) {
        WakepointHeader()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp)
                .background(WakepointCanvas),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .border(2.dp, Color(0xFFCBCBCB), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFC7BCB0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profileName.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        color = WakepointInk
                    )
                }
            }
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleLarge,
                color = WakepointInk,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = profileEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFD8DDE3))
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
            color = WakepointDanger,
            showChevron = false,
            onClick = onLogout
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(WakepointParchment.copy(alpha = 0.48f))
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = WakepointInk,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                modifier = Modifier.weight(1f)
            )
            if (showChevron) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = WakepointMuted
                )
            }
        }
        HorizontalDivider(
            color = Color(0xFFD8DDE3),
            modifier = Modifier.padding(start = 24.dp, end = 16.dp)
        )
    }
}
