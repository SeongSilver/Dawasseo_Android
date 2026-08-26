package com.wakepoint.app.feature.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.SettingsRow
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointIconLight
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointMuted
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextDisabled
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTextTertiary

/** 리스트 하단이 떠 있는 탭 바에 가리지 않도록 확보하는 구조적 스크롤 여백. */
private val BOTTOM_LIST_CLEARANCE = 120.dp

@Composable
fun ProfileScreen(
    onOpenSoundList: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val profileName = uiState.nickname.ifBlank { stringResource(R.string.profile_name) }
    val profileEmail = uiState.email.ifBlank { stringResource(R.string.profile_email) }
    val supportEmail = stringResource(R.string.profile_support_email)
    val supportSubject = stringResource(R.string.profile_support_subject)
    val supportUnavailable = stringResource(R.string.profile_support_unavailable)
    var showEditNickname by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground)
    ) {
        WakepointPageHeader(title = stringResource(R.string.profile_title))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(top = WakepointSpacing.sectionTitleGap, bottom = BOTTOM_LIST_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)
        ) {
            ProfileCard(
                name = profileName,
                email = profileEmail,
                onClick = { showEditNickname = true }
            )
            StatsCard(
                wakeupCount = uiState.wakeupCount,
                activeAlarmCount = uiState.activeAlarmCount,
                friendCount = uiState.friendCount
            )
            SettingsGroupCard(
                onOpenSoundList = onOpenSoundList,
                onOpenNotificationSettings = { context.openAppNotificationSettings() },
                onOpenLocationSettings = { context.openAppDetailsSettings() },
                onOpenSupport = { context.openSupportEmail(supportEmail, supportSubject, supportUnavailable) }
            )
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
                    .padding(top = WakepointSpacing.sm)
            )
        }
    }

    if (showEditNickname) {
        EditNicknameDialog(
            initialNickname = uiState.nickname,
            isSaving = uiState.isSavingNickname,
            errorMessage = uiState.nicknameErrorMessage,
            onDismiss = {
                showEditNickname = false
                viewModel.consumeNicknameError()
            },
            onConfirm = { newName -> viewModel.updateNickname(newName) }
        )
    }
}

@Composable
private fun EditNicknameDialog(
    initialNickname: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nickname by remember { mutableStateOf(initialNickname) }
    var pendingClose by remember { mutableStateOf(false) }

    // 저장이 끝난 시점(저장 중 → 완료)에 에러가 없으면 다이얼로그를 닫는다.
    LaunchedEffect(isSaving) {
        if (isSaving) {
            pendingClose = true
        } else if (pendingClose) {
            pendingClose = false
            if (errorMessage == null) onDismiss()
        }
    }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = WakepointShape.card,
            color = WakepointCanvas
        ) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
            ) {
                Text(
                    text = stringResource(R.string.profile_edit_nickname_title),
                    color = WakepointInk,
                    style = MaterialTheme.typography.titleLarge
                )
                WakepointTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = stringResource(R.string.profile_nickname_hint),
                    readOnly = isSaving
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = WakepointMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
                ) {
                    WakepointSecondaryButton(
                        text = stringResource(R.string.alarm_cancel),
                        enabled = !isSaving,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointButton(
                        text = stringResource(R.string.alarm_save),
                        enabled = !isSaving && nickname.trim().isNotBlank(),
                        onClick = { onConfirm(nickname.trim()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(name: String, email: String, onClick: () -> Unit) {
    WakepointCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
        ) {
            WakepointAvatar(name = name, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = WakepointInk
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextTertiary
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
private fun StatsCard(
    wakeupCount: Int,
    activeAlarmCount: Int,
    friendCount: Int
) {
    WakepointCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                value = wakeupCount.toString(),
                label = stringResource(R.string.profile_stat_wakeups),
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatColumn(
                value = activeAlarmCount.toString(),
                label = stringResource(R.string.profile_stat_active),
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatColumn(
                value = friendCount.toString(),
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
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        Text(
            // 통계 숫자는 22(titleLarge) — 화면 타이틀 28과 충돌 방지
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = WakepointInk
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WakepointTextTertiary,
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
private fun SettingsGroupCard(
    onOpenSoundList: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenSupport: () -> Unit
) {
    WakepointCard(contentPadding = 0) {
        SettingsRow(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.profile_notification),
            onClick = onOpenNotificationSettings
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = WakepointSpacing.cardPadding))
        SettingsRow(
            icon = Icons.Rounded.LocationOn,
            title = stringResource(R.string.profile_location),
            onClick = onOpenLocationSettings
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = WakepointSpacing.cardPadding))
        SettingsRow(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            title = stringResource(R.string.profile_sound),
            onClick = onOpenSoundList
        )
        HorizontalDivider(color = WakepointDivider, modifier = Modifier.padding(start = WakepointSpacing.cardPadding))
        SettingsRow(
            icon = Icons.AutoMirrored.Rounded.HelpOutline,
            title = stringResource(R.string.profile_support),
            onClick = onOpenSupport
        )
    }
}

/** 앱 알림 설정 화면(채널 목록)으로 이동. */
private fun Context.openAppNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    runCatching { startActivity(intent) }.onFailure {
        openAppDetailsSettings()
    }
}

/** 앱 상세 설정 화면(권한 관리 포함)으로 이동. */
private fun Context.openAppDetailsSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    runCatching { startActivity(intent) }
}

/** 고객센터 문의 메일 작성. */
private fun Context.openSupportEmail(email: String, subject: String, unavailableMessage: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, unavailableMessage, Toast.LENGTH_SHORT).show()
    }
}
