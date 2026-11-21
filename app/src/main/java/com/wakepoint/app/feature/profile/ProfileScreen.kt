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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Train
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.R
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.SettingsRow
import com.wakepoint.app.core.design.WakepointAvatar
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointDivider
import com.wakepoint.app.core.design.WakepointHeader
import com.wakepoint.app.core.design.WakepointMessage
import com.wakepoint.app.core.design.WakepointMessageKind
import com.wakepoint.app.core.design.WakepointScreenPreviews
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextField
import com.wakepoint.app.core.design.WakepointTheme

/** 리스트 하단이 떠 있는 탭 바(80dp)에 가리지 않도록 확보하는 구조적 스크롤 여백. */
private val BOTTOM_LIST_CLEARANCE = 80.dp

/** 프로필 영역 아바타 한 변 (design-handoff `Profile` · 64dp 정사각 · corner-large). */
private const val PROFILE_AVATAR_SIZE = 64

@Composable
fun ProfileScreen(
    onOpenSoundList: () -> Unit,
    onOpenMyPlaces: () -> Unit,
    onOpenLastTrain: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val supportEmail = stringResource(R.string.profile_support_email)
    val supportSubject = stringResource(R.string.profile_support_subject)
    val supportUnavailable = stringResource(R.string.profile_support_unavailable)
    var showEditNickname by remember { mutableStateOf(false) }

    ProfileContent(
        name = uiState.nickname.ifBlank { stringResource(R.string.profile_name) },
        email = uiState.email.ifBlank { stringResource(R.string.profile_email) },
        wakeupCount = uiState.wakeupCount,
        activeAlarmCount = uiState.activeAlarmCount,
        friendCount = uiState.friendCount,
        onEditNickname = { showEditNickname = true },
        onOpenMyPlaces = onOpenMyPlaces,
        onOpenLastTrain = onOpenLastTrain,
        onOpenNotificationSettings = { context.openAppNotificationSettings() },
        onOpenLocationSettings = { context.openAppDetailsSettings() },
        onOpenSoundList = onOpenSoundList,
        onOpenSupport = { context.openSupportEmail(supportEmail, supportSubject, supportUnavailable) },
        onLogout = onLogout
    )

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

/** 상태 없는 본문 — 프리뷰용. */
@Composable
private fun ProfileContent(
    name: String,
    email: String,
    wakeupCount: Int,
    activeAlarmCount: Int,
    friendCount: Int,
    onEditNickname: () -> Unit,
    onOpenMyPlaces: () -> Unit,
    onOpenLastTrain: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenSoundList: () -> Unit,
    onOpenSupport: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        WakepointHeader()
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(top = WakepointSpacing.sm, bottom = BOTTOM_LIST_CLEARANCE),
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
        ) {
            ProfileIdentity(
                name = name,
                email = email,
                onClick = onEditNickname
            )
            WakepointDivider()
            StatsRow(
                wakeupCount = wakeupCount,
                activeAlarmCount = activeAlarmCount,
                friendCount = friendCount
            )
            SettingsGroupCard(
                onOpenSoundList = onOpenSoundList,
                onOpenMyPlaces = onOpenMyPlaces,
                onOpenLastTrain = onOpenLastTrain,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenLocationSettings = onOpenLocationSettings,
                onOpenSupport = onOpenSupport
            )
            WakepointCard(contentPadding = 0, filled = false) {
                SettingsRow(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = stringResource(R.string.profile_logout),
                    showChevron = false,
                    onClick = onLogout
                )
            }
            Text(
                text = stringResource(R.string.profile_version),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
            shape = WakepointShape.dialog,
            color = LocalWakepointColors.current.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.formHorizontal),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
            ) {
                Text(
                    text = stringResource(R.string.profile_edit_nickname_title),
                    color = LocalWakepointColors.current.text,
                    style = MaterialTheme.typography.headlineSmall
                )
                WakepointTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = stringResource(R.string.profile_nickname_hint),
                    readOnly = isSaving
                )
                if (errorMessage != null) {
                    WakepointMessage(
                        text = errorMessage,
                        kind = WakepointMessageKind.Error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
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

/** 프로필 영역 — 아바타 64(모서리 16) + 이름 titleLarge + 메일 bodyMedium. */
@Composable
private fun ProfileIdentity(name: String, email: String, onClick: () -> Unit) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(WakepointSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        WakepointAvatar(
            name = name,
            size = PROFILE_AVATAR_SIZE,
            shape = WakepointShape.card
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = colors.text
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary
        )
    }
}

@Composable
private fun StatsRow(
    wakeupCount: Int,
    activeAlarmCount: Int,
    friendCount: Int
) {
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

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.xs)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = LocalWakepointColors.current.text
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalWakepointColors.current.textSecondary,
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
            .background(LocalWakepointColors.current.line)
    )
}

@Composable
private fun SettingsGroupCard(
    onOpenSoundList: () -> Unit,
    onOpenMyPlaces: () -> Unit,
    onOpenLastTrain: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenSupport: () -> Unit
) {
    WakepointCard(contentPadding = 0) {
        // v2 STEP2 (2-1) 마이 진입점: 내 장소 관리
        SettingsRow(
            icon = Icons.Rounded.Map,
            title = stringResource(R.string.profile_my_places),
            onClick = onOpenMyPlaces
        )
        WakepointDivider(inset = true)
        // v2 STEP4 마이 진입점: 막차 알람(모의 데이터 미리보기)
        SettingsRow(
            icon = Icons.Rounded.Train,
            title = stringResource(R.string.profile_last_train),
            onClick = onOpenLastTrain
        )
        WakepointDivider(inset = true)
        SettingsRow(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.profile_notification),
            onClick = onOpenNotificationSettings
        )
        WakepointDivider(inset = true)
        SettingsRow(
            icon = Icons.Rounded.LocationOn,
            title = stringResource(R.string.profile_location),
            onClick = onOpenLocationSettings
        )
        WakepointDivider(inset = true)
        SettingsRow(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            title = stringResource(R.string.profile_sound),
            onClick = onOpenSoundList
        )
        WakepointDivider(inset = true)
        SettingsRow(
            icon = Icons.Rounded.ChatBubbleOutline,
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

@WakepointScreenPreviews
@Composable
private fun ProfileScreenPreview() {
    WakepointTheme {
        ProfileContent(
            name = "김민지",
            email = "minji@example.com",
            wakeupCount = 12,
            activeAlarmCount = 2,
            friendCount = 5,
            onEditNickname = {},
            onOpenMyPlaces = {},
            onOpenLastTrain = {},
            onOpenNotificationSettings = {},
            onOpenLocationSettings = {},
            onOpenSoundList = {},
            onOpenSupport = {},
            onLogout = {}
        )
    }
}
