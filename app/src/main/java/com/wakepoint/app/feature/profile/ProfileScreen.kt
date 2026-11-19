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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R
import com.wakepoint.app.core.design.MessageText
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointTextField

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    fallbackEmail: String,
    onNicknameDraftChange: (String) -> Unit,
    onStartEditNickname: () -> Unit,
    onCancelEditNickname: () -> Unit,
    onSaveNickname: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = uiState.profile
    val loadingLabel = stringResource(R.string.profile_loading)
    val unknownName = stringResource(R.string.profile_name_unknown)
    val unknownEmail = stringResource(R.string.profile_email_unknown)
    val profileName = profile?.nickname?.ifBlank { null }
        ?: if (uiState.isLoading) loadingLabel else unknownName
    val profileEmail = profile?.email?.ifBlank { null }
        ?: fallbackEmail.ifBlank { unknownEmail }

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
            if (!uiState.isEditingNickname) {
                uiState.errorMessage?.let { message ->
                    MessageText(text = message, isError = true)
                }
            }
        }
        HorizontalDivider()
        ProfileMenuItem(
            icon = Icons.Rounded.Edit,
            text = stringResource(R.string.profile_edit_nickname),
            onClick = onStartEditNickname
        )
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

    if (uiState.isEditingNickname) {
        AlertDialog(
            onDismissRequest = onCancelEditNickname,
            title = { Text(text = stringResource(R.string.profile_edit_nickname)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WakepointTextField(
                        value = uiState.nicknameDraft,
                        onValueChange = onNicknameDraftChange,
                        placeholder = stringResource(R.string.profile_nickname_hint),
                        readOnly = uiState.isSaving
                    )
                    uiState.errorMessage?.let { message ->
                        MessageText(text = message, isError = true)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSaveNickname, enabled = !uiState.isSaving) {
                    Text(
                        text = if (uiState.isSaving) {
                            stringResource(R.string.auth_processing)
                        } else {
                            stringResource(R.string.alarm_save)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelEditNickname, enabled = !uiState.isSaving) {
                    Text(text = stringResource(R.string.alarm_cancel))
                }
            }
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
